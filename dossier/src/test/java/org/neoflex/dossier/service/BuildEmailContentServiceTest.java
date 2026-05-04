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
import org.neoflex.dossier.dto.DealDocumentDto;
import org.neoflex.dossier.enums.Theme;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Тесты сервиса BuildEmailContentService")
@ExtendWith(MockitoExtension.class)
class BuildEmailContentServiceTest {

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private BuildEmailContentService buildEmailContentService;

    private EmailMessage emailMessage;
    private UUID statementId;
    private String dealUrl;

    @BeforeEach
    void setUp() {
        statementId = UUID.randomUUID();
        dealUrl = "http://localhost:8081";

        buildEmailContentService = new BuildEmailContentService(templateEngine);
        java.lang.reflect.Field field = org.springframework.util.ReflectionUtils.findField(BuildEmailContentService.class, "dealUrl");
        field.setAccessible(true);
        org.springframework.util.ReflectionUtils.setField(field, buildEmailContentService, dealUrl);

        emailMessage = EmailMessage.builder()
                .address("test@example.com")
                .theme(Theme.CREATE_DOCUMENTS)
                .statementId(statementId)
                .text("Test message")
                .build();
    }

    @Test
    @DisplayName("Построение email контента для темы CREATE_DOCUMENTS")
    void buildEmailContentShouldGenerateHtmlForCreateDocuments() {
        String expectedHtml = "<html>Test content</html>";
        when(templateEngine.process(eq("email/create-documents"), any())).thenReturn(expectedHtml);

        String result = buildEmailContentService.buildEmailContent(emailMessage);

        assertEquals(expectedHtml, result);
        verify(templateEngine).process(eq("email/create-documents"), any());
    }

    @Test
    @DisplayName("Построение email контента для темы FINISH_REGISTRATION")
    void buildEmailContentShouldGenerateHtmlForFinishRegistration() {
        emailMessage = EmailMessage.builder()
                .address("test@example.com")
                .theme(Theme.FINISH_REGISTRATION)
                .statementId(statementId)
                .text("Finish registration")
                .build();

        String expectedHtml = "<html>Finish content</html>";
        when(templateEngine.process(eq("email/finish-registration"), any())).thenReturn(expectedHtml);

        String result = buildEmailContentService.buildEmailContent(emailMessage);

        assertEquals(expectedHtml, result);
        verify(templateEngine).process(eq("email/finish-registration"), any());
    }

    @Test
    @DisplayName("Построение email контента для темы SEND_DOCUMENTS")
    void buildEmailContentShouldGenerateHtmlForSendDocuments() {
        emailMessage = EmailMessage.builder()
                .address("test@example.com")
                .theme(Theme.SEND_DOCUMENTS)
                .statementId(statementId)
                .text("Send documents")
                .build();

        String expectedHtml = "<html>Send documents content</html>";
        when(templateEngine.process(eq("email/send-documents"), any())).thenReturn(expectedHtml);

        String result = buildEmailContentService.buildEmailContent(emailMessage);

        assertEquals(expectedHtml, result);
        verify(templateEngine).process(eq("email/send-documents"), any());
    }

    @Test
    @DisplayName("Построение email контента для темы SEND_SES с кодом подтверждения")
    void buildEmailContentShouldGenerateHtmlForSendSesWithCode() {
        String sesCode = "123456";
        emailMessage = EmailMessage.builder()
                .address("test@example.com")
                .theme(Theme.SEND_SES)
                .statementId(statementId)
                .text(sesCode)
                .build();

        String expectedHtml = "<html>SES code: 123456</html>";
        when(templateEngine.process(eq("email/send-ses"), any())).thenReturn(expectedHtml);

        String result = buildEmailContentService.buildEmailContent(emailMessage);

        assertEquals(expectedHtml, result);
        verify(templateEngine).process(eq("email/send-ses"), any());
    }

    @Test
    @DisplayName("Построение email контента для темы CREDIT_ISSUED")
    void buildEmailContentShouldGenerateHtmlForCreditIssued() {
        emailMessage = EmailMessage.builder()
                .address("test@example.com")
                .theme(Theme.CREDIT_ISSUED)
                .statementId(statementId)
                .text("Credit issued")
                .build();

        String expectedHtml = "<html>Credit issued content</html>";
        when(templateEngine.process(eq("email/credit-issued"), any())).thenReturn(expectedHtml);

        String result = buildEmailContentService.buildEmailContent(emailMessage);

        assertEquals(expectedHtml, result);
        verify(templateEngine).process(eq("email/credit-issued"), any());
    }

    @Test
    @DisplayName("Построение email контента для темы STATEMENT_DENIED")
    void buildEmailContentShouldGenerateHtmlForStatementDenied() {
        emailMessage = EmailMessage.builder()
                .address("test@example.com")
                .theme(Theme.STATEMENT_DENIED)
                .statementId(statementId)
                .text("Statement denied")
                .build();

        String expectedHtml = "<html>Statement denied content</html>";
        when(templateEngine.process(eq("email/statement-denied"), any())).thenReturn(expectedHtml);

        String result = buildEmailContentService.buildEmailContent(emailMessage);

        assertEquals(expectedHtml, result);
        verify(templateEngine).process(eq("email/statement-denied"), any());
    }
}