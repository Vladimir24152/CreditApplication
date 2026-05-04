package org.neoflex.dossier.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neoflex.dossier.client.DealClientService;
import org.neoflex.dossier.dto.DealDocumentDto;
import org.neoflex.dossier.dto.EmailMessage;
import org.neoflex.dossier.enums.Theme;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Тесты сервиса DocumentService")
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DealClientService dealClientService;

    @Mock
    private PdfGenerationService pdfGenerationService;

    @Mock
    private EmailService emailService;

    @Mock
    private BuildEmailContentService buildEmailContentService;

    @InjectMocks
    private DocumentService documentService;

    private EmailMessage emailMessage;
    private UUID statementId;
    private DealDocumentDto dealDocumentDto;
    private byte[] pdfBytes;

    @BeforeEach
    void setUp() {
        statementId = UUID.randomUUID();
        pdfBytes = "PDF_CONTENT".getBytes();

        emailMessage = EmailMessage.builder()
                .address("test@example.com")
                .theme(Theme.SEND_DOCUMENTS)
                .statementId(statementId)
                .text("Documents ready")
                .build();

        dealDocumentDto = DealDocumentDto.builder()
                .statementId(statementId)
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("client@example.com")
                .amount(BigDecimal.valueOf(1000000))
                .term(12)
                .build();
    }

    @Test
    @DisplayName("Успешное создание документа и отправка письма с PDF вложением")
    void createDocumentShouldGeneratePdfAndSendEmailWithAttachment() {
        String expectedHtml = "<html>Email content</html>";

        when(dealClientService.getDealDocument(statementId)).thenReturn(dealDocumentDto);
        when(pdfGenerationService.generateCreditAgreement(dealDocumentDto)).thenReturn(Optional.of(pdfBytes));
        when(buildEmailContentService.buildEmailContent(emailMessage)).thenReturn(expectedHtml);
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString(), any(), anyString());

        documentService.createDocument(emailMessage);

        verify(dealClientService).getDealDocument(statementId);
        verify(pdfGenerationService).generateCreditAgreement(dealDocumentDto);
        verify(buildEmailContentService).buildEmailContent(emailMessage);
        verify(emailService).sendEmail(
                eq(dealDocumentDto.getEmail()),
                eq("Кредитный договор №" + statementId),
                eq(expectedHtml),
                eq(pdfBytes),
                eq("credit_agreement_" + statementId + ".pdf")
        );
    }

    @Test
    @DisplayName("При ошибке генерации PDF отправляется письмо без вложения")
    void createDocumentShouldSendEmailWithoutAttachmentWhenPdfGenerationFails() {
        when(dealClientService.getDealDocument(statementId)).thenReturn(dealDocumentDto);
        when(pdfGenerationService.generateCreditAgreement(dealDocumentDto)).thenReturn(Optional.empty());
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        documentService.createDocument(emailMessage);

        verify(dealClientService).getDealDocument(statementId);
        verify(pdfGenerationService).generateCreditAgreement(dealDocumentDto);
        verify(emailService).sendEmail(
                eq(dealDocumentDto.getEmail()),
                eq("Кредитный договор №" + statementId),
                anyString()
        );
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), any(), anyString());
    }
}