package org.neoflex.dossier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.dossier.dto.EmailMessage;
import org.neoflex.dossier.enums.Theme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildEmailContentService {

    private final TemplateEngine templateEngine;

    @Value("${external.deal.url:http://localhost:8081}")
    private String dealUrl;

    public String buildEmailContent(EmailMessage message) {
        Context context = new Context();
        context.setVariable("statementId", message.getStatementId());
        context.setVariable("text", message.getText());
        context.setVariable("dealUrl", dealUrl);

        if (message.getTheme() == Theme.SEND_SES) {
            context.setVariable("code", message.getText());
        }

        String templateName = getTemplateName(message.getTheme());
        return templateEngine.process("email/" + templateName, context);
    }

    private String getTemplateName(Theme theme) {
        return switch (theme) {
            case FINISH_REGISTRATION -> "finish-registration";
            case CREATE_DOCUMENTS -> "create-documents";
            case SEND_DOCUMENTS -> "send-documents";
            case SEND_SES -> "send-ses";
            case CREDIT_ISSUED -> "credit-issued";
            case STATEMENT_DENIED -> "statement-denied";
        };
    }
}