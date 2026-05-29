package org.neoflex.dossier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.dossier.client.DealClientService;
import org.neoflex.dossier.dto.DealDocumentDto;
import org.neoflex.dossier.dto.EmailMessage;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DealClientService dealClientService;
    private final PdfGenerationService pdfGenerationService;
    private final EmailService emailService;
    private final EmailContentBuilder emailContentBuilder;
    private final TemplateEngine templateEngine;

    public void createDocument(EmailMessage emailMessage) {
        UUID statementId = emailMessage.getStatementId();
        DealDocumentDto dto = dealClientService.getDealDocument(statementId);

        Optional<byte[]> document = pdfGenerationService.generateCreditAgreement(dto);
        if (document.isPresent()) {
            emailService.sendEmail(
                    dto.getEmail(),
                    "Кредитный договор №" + statementId,
                    emailContentBuilder.buildEmailContent(emailMessage),
                    document.get(),
                    "credit_agreement_" + statementId + ".pdf"
            );
            log.info("Документ и письмо отправлены клиенту {}", dto.getEmail());
        } else {
            String fallbackHtmlBody = buildFallEmailHtml(dto);
            emailService.sendEmail(
                    dto.getEmail(),
                    "Кредитный договор №" + statementId,
                    fallbackHtmlBody
            );
            log.warn("PDF не был сгенерирован для заявки {}, отправлено письмо без вложения", statementId);
        }
    }

    private String buildFallEmailHtml(DealDocumentDto data) {
        Context context = new Context();
        context.setVariable("firstName", data.getFirstName());
        context.setVariable("lastName", data.getLastName());
        context.setVariable("statementId", data.getStatementId().toString());
        return templateEngine.process("email/fallback", context);
    }
}