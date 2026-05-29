package org.neoflex.dossier.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neoflex.dossier.dto.EmailMessage;
import org.neoflex.dossier.enums.Theme;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.internet.MimeMessage;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Тесты сервиса EmailService")
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailContentBuilder emailContentBuilder;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private EmailMessage emailMessage;
    private UUID statementId;
    private String expectedHtml;
    private String fromEmail;

    @BeforeEach
    void setUp() throws Exception {
        statementId = UUID.randomUUID();
        expectedHtml = "<html>Test email content</html>";
        fromEmail = "test@example.com";

        emailMessage = EmailMessage.builder()
                .address("client@example.com")
                .theme(Theme.CREATE_DOCUMENTS)
                .statementId(statementId)
                .text("Test message")
                .build();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);
    }

    @Test
    @DisplayName("Успешная отправка email без вложения")
    void sendEmailShouldSendSimpleEmailSuccessfully() throws Exception {
        when(emailContentBuilder.buildEmailContent(emailMessage)).thenReturn(expectedHtml);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(emailMessage);

        verify(emailContentBuilder).buildEmailContent(emailMessage);
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Успешная отправка email с PDF вложением")
    void sendEmailWithAttachmentShouldSendEmailSuccessfully() throws Exception {
        byte[] pdfBytes = "PDF_CONTENT".getBytes();
        String fileName = "document.pdf";
        String to = "client@example.com";
        String subject = "Test subject";
        String text = "Test text";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(to, subject, text, pdfBytes, fileName);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Успешная отправка простого email")
    void sendSimpleEmailShouldSendEmailSuccessfully() throws Exception {
        String to = "client@example.com";
        String subject = "Test subject";
        String text = "Test text";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(to, subject, text);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Отправка email с темой FINISH_REGISTRATION")
    void sendEmailWithFinishRegistrationTheme() throws Exception {
        emailMessage = EmailMessage.builder()
                .address("client@example.com")
                .theme(Theme.FINISH_REGISTRATION)
                .statementId(statementId)
                .text("Finish registration")
                .build();

        when(emailContentBuilder.buildEmailContent(emailMessage)).thenReturn(expectedHtml);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(emailMessage);

        verify(emailContentBuilder).buildEmailContent(emailMessage);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Отправка email с темой CREDIT_ISSUED")
    void sendEmailWithCreditIssuedTheme() throws Exception {
        emailMessage = EmailMessage.builder()
                .address("client@example.com")
                .theme(Theme.CREDIT_ISSUED)
                .statementId(statementId)
                .text("Credit issued")
                .build();

        when(emailContentBuilder.buildEmailContent(emailMessage)).thenReturn(expectedHtml);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(emailMessage);

        verify(emailContentBuilder).buildEmailContent(emailMessage);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Отправка email с темой CREATE_DOCUMENTS")
    void sendEmailWithCreateDocumentsTheme() throws Exception {
        emailMessage = EmailMessage.builder()
                .address("client@example.com")
                .theme(Theme.CREATE_DOCUMENTS)
                .statementId(statementId)
                .text("Create documents")
                .build();

        when(emailContentBuilder.buildEmailContent(emailMessage)).thenReturn(expectedHtml);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(emailMessage);

        verify(emailContentBuilder).buildEmailContent(emailMessage);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Отправка email с темой SEND_DOCUMENTS")
    void sendEmailWithSendDocumentsTheme() throws Exception {
        emailMessage = EmailMessage.builder()
                .address("client@example.com")
                .theme(Theme.SEND_DOCUMENTS)
                .statementId(statementId)
                .text("Send documents")
                .build();

        when(emailContentBuilder.buildEmailContent(emailMessage)).thenReturn(expectedHtml);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(emailMessage);

        verify(emailContentBuilder).buildEmailContent(emailMessage);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Отправка email с темой SEND_SES")
    void sendEmailWithSendSesTheme() throws Exception {
        emailMessage = EmailMessage.builder()
                .address("client@example.com")
                .theme(Theme.SEND_SES)
                .statementId(statementId)
                .text("123456")
                .build();

        when(emailContentBuilder.buildEmailContent(emailMessage)).thenReturn(expectedHtml);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(emailMessage);

        verify(emailContentBuilder).buildEmailContent(emailMessage);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Отправка email с темой STATEMENT_DENIED")
    void sendEmailWithStatementDeniedTheme() throws Exception {
        emailMessage = EmailMessage.builder()
                .address("client@example.com")
                .theme(Theme.STATEMENT_DENIED)
                .statementId(statementId)
                .text("Statement denied")
                .build();

        when(emailContentBuilder.buildEmailContent(emailMessage)).thenReturn(expectedHtml);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(emailMessage);

        verify(emailContentBuilder).buildEmailContent(emailMessage);
        verify(mailSender).send(any(MimeMessage.class));
    }
}