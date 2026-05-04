package org.neoflex.dossier.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neoflex.dossier.dto.EmailMessage;
import org.neoflex.dossier.enums.Theme;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@DisplayName("Тесты сервиса KafkaConsumerService")
@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    private EmailMessage emailMessage;
    private UUID statementId;

    @BeforeEach
    void setUp() {
        statementId = UUID.randomUUID();

        emailMessage = EmailMessage.builder()
                .address("test@example.com")
                .theme(Theme.FINISH_REGISTRATION)
                .statementId(statementId)
                .text("Test message")
                .build();
    }

    @Test
    @DisplayName("Обработка сообщения из топика finish-registration")
    void handleFinishRegistrationShouldProcessMessage() {
        doNothing().when(emailService).sendEmail(any());

        kafkaConsumerService.handleFinishRegistration(emailMessage);

        verify(emailService).sendEmail(emailMessage);
    }

    @Test
    @DisplayName("Обработка сообщения из топика create-documents")
    void handleCreateDocumentsShouldProcessMessage() {
        doNothing().when(emailService).sendEmail(any());

        kafkaConsumerService.handleCreateDocuments(emailMessage);

        verify(emailService).sendEmail(emailMessage);
    }

    @Test
    @DisplayName("Обработка сообщения из топика send-documents с созданием документа")
    void handleSendDocumentsShouldCreateDocument() {
        doNothing().when(documentService).createDocument(any());

        kafkaConsumerService.handleSendDocuments(emailMessage);

        verify(documentService).createDocument(emailMessage);
    }

    @Test
    @DisplayName("Обработка сообщения из топика send-ses")
    void handleSendSesShouldProcessMessage() {
        emailMessage = EmailMessage.builder()
                .address("test@example.com")
                .theme(Theme.SEND_SES)
                .statementId(statementId)
                .text("123456")
                .build();

        doNothing().when(emailService).sendEmail(any());

        kafkaConsumerService.handleSendSes(emailMessage);

        verify(emailService).sendEmail(emailMessage);
    }

    @Test
    @DisplayName("Обработка сообщения из топика credit-issued")
    void handleCreditIssuedShouldProcessMessage() {
        emailMessage = EmailMessage.builder()
                .address("test@example.com")
                .theme(Theme.CREDIT_ISSUED)
                .statementId(statementId)
                .text("Credit issued")
                .build();

        doNothing().when(emailService).sendEmail(any());

        kafkaConsumerService.handleCreditIssued(emailMessage);

        verify(emailService).sendEmail(emailMessage);
    }

    @Test
    @DisplayName("Обработка сообщения из топика statement-denied")
    void handleStatementDeniedShouldProcessMessage() {
        emailMessage = EmailMessage.builder()
                .address("test@example.com")
                .theme(Theme.STATEMENT_DENIED)
                .statementId(statementId)
                .text("Statement denied")
                .build();

        doNothing().when(emailService).sendEmail(any());

        kafkaConsumerService.handleStatementDenied(emailMessage);

        verify(emailService).sendEmail(emailMessage);
    }
}