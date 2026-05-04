package org.neoflex.deal.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neoflex.deal.dto.EmailMessage;
import org.neoflex.deal.exception.CodeVerificationException;
import org.neoflex.deal.model.Client;
import org.neoflex.deal.model.Credit;
import org.neoflex.deal.model.Statement;
import org.neoflex.deal.model.enums.ApplicationStatus;
import org.neoflex.deal.model.enums.CreditStatus;
import org.neoflex.deal.model.enums.Theme;
import org.neoflex.deal.repository.CreditRepository;
import org.neoflex.deal.repository.StatementRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Тесты сервиса DocumentService")
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private DocumentService documentService;

    private UUID statementId;
    private Statement statement;
    private Credit credit;
    private Client client;
    private String sesCode;

    @BeforeEach
    void setUp() {
        statementId = UUID.randomUUID();
        sesCode = "123456";

        client = Client.builder()
                .clientId(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Petrov")
                .email("ivan@example.com")
                .build();

        credit = Credit.builder()
                .creditId(UUID.randomUUID())
                .amount(java.math.BigDecimal.valueOf(1000000))
                .creditStatus(CreditStatus.CALCULATED)
                .build();

        statement = Statement.builder()
                .statementId(statementId)
                .client(client)
                .credit(credit)
                .status(ApplicationStatus.DOCUMENT_SIGNED)
                .sesCode(sesCode)
                .build();
    }

    @Test
    @DisplayName("Успешная отправка документов через Kafka")
    void sendDocumentsShouldSendKafkaMessageSuccessfully() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));

        documentService.sendDocuments(statementId);

        ArgumentCaptor<EmailMessage> messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(kafkaProducerService).send(messageCaptor.capture());

        EmailMessage sentMessage = messageCaptor.getValue();
        assertEquals(client.getEmail(), sentMessage.getAddress());
        assertEquals(Theme.SEND_DOCUMENTS, sentMessage.getTheme());
        assertEquals(statementId, sentMessage.getStatementId());
        assertNotNull(sentMessage.getText());

        verify(statementRepository).findById(statementId);
    }

    @Test
    @DisplayName("При несуществующем statementId при отправке документов выбрасывается EntityNotFoundException")
    void sendDocumentsShouldThrowExceptionWhenStatementNotFound() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> documentService.sendDocuments(statementId));

        assertTrue(exception.getMessage().contains(statementId.toString()));
        verify(statementRepository).findById(statementId);
        verify(kafkaProducerService, never()).send(any());
    }

    @Test
    @DisplayName("Успешное подписание документов с генерацией SES кода")
    void signDocumentsShouldGenerateSesCodeAndSendKafkaMessage() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));

        documentService.signDocuments(statementId);

        ArgumentCaptor<Statement> statementCaptor = ArgumentCaptor.forClass(Statement.class);
        verify(statementRepository).save(statementCaptor.capture());

        Statement savedStatement = statementCaptor.getValue();
        assertNotNull(savedStatement.getSesCode());

        ArgumentCaptor<EmailMessage> messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(kafkaProducerService).send(messageCaptor.capture());

        EmailMessage sentMessage = messageCaptor.getValue();
        assertEquals(client.getEmail(), sentMessage.getAddress());
        assertEquals(Theme.SEND_SES, sentMessage.getTheme());
        assertEquals(statementId, sentMessage.getStatementId());
        assertEquals(savedStatement.getSesCode(), sentMessage.getText());

        verify(statementRepository).findById(statementId);
    }

    @Test
    @DisplayName("При несуществующем statementId при подписании документов выбрасывается EntityNotFoundException")
    void signDocumentsShouldThrowExceptionWhenStatementNotFound() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> documentService.signDocuments(statementId));

        assertTrue(exception.getMessage().contains(statementId.toString()));
        verify(statementRepository).findById(statementId);
        verify(statementRepository, never()).save(any());
        verify(kafkaProducerService, never()).send(any());
    }

    @Test
    @DisplayName("Успешная верификация кода и выдача кредита")
    void verifyCodeShouldVerifySuccessfullyAndIssueCredit() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));

        documentService.verifyCode(statementId, sesCode);

        assertEquals(CreditStatus.ISSUED, statement.getCredit().getCreditStatus());
        assertEquals(ApplicationStatus.CREDIT_ISSUED, statement.getStatus());

        verify(creditRepository).save(credit);
        verify(statementRepository).save(statement);

        ArgumentCaptor<EmailMessage> messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(kafkaProducerService).send(messageCaptor.capture());

        EmailMessage sentMessage = messageCaptor.getValue();
        assertEquals(client.getEmail(), sentMessage.getAddress());
        assertEquals(Theme.CREDIT_ISSUED, sentMessage.getTheme());
        assertEquals(statementId, sentMessage.getStatementId());
    }

    @Test
    @DisplayName("При несуществующем statementId при верификации выбрасывается EntityNotFoundException")
    void verifyCodeShouldThrowExceptionWhenStatementNotFound() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> documentService.verifyCode(statementId, sesCode));

        assertTrue(exception.getMessage().contains(statementId.toString()));
        verify(statementRepository).findById(statementId);
        verify(creditRepository, never()).save(any());
        verify(statementRepository, never()).save(any());
        verify(kafkaProducerService, never()).send(any());
    }

    @Test
    @DisplayName("При неверном коде верификации выбрасывается CodeVerificationException")
    void verifyCodeShouldThrowExceptionWhenCodeIsInvalid() {
        String invalidCode = "000000";
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));

        CodeVerificationException exception = assertThrows(CodeVerificationException.class,
                () -> documentService.verifyCode(statementId, invalidCode));

        assertTrue(exception.getMessage().contains(statementId.toString()));
        assertEquals(CreditStatus.CALCULATED, statement.getCredit().getCreditStatus());
        assertEquals(ApplicationStatus.DOCUMENT_SIGNED, statement.getStatus());

        verify(creditRepository, never()).save(any());
        verify(statementRepository, never()).save(any());
        verify(kafkaProducerService, never()).send(any());
    }
}