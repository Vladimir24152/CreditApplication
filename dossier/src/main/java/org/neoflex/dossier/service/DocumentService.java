package org.neoflex.dossier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.creditapplicationsupportstarter.dto.EmailMessage;
import org.neoflex.dossier.client.DealClientService;
import org.neoflex.dossier.dto.DealDocumentDto;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DealClientService dealClientService;
    private final PdfGenerationService pdfGenerationService;
    private final EmailService emailService;

    public void createDocument(EmailMessage emailMessage) {
        UUID statementId = emailMessage.getStatementId();
        DealDocumentDto dto = dealClientService.getDealDocument(statementId);

        Optional<byte[]> maybePdf = pdfGenerationService.generateCreditAgreement(dto);
        if (maybePdf.isPresent()) {
            String htmlBody = buildEmailHtml(dto);

            emailService.sendEmailWithDocument(
                    dto.getEmail(),
                    "Кредитный договор №" + statementId,
                    htmlBody,
                    maybePdf.get(),
                    "credit_agreement_" + statementId + ".pdf"
            );
            log.info("Документ и письмо отправлены клиенту {}", dto.getEmail());
        } else {
            String fallbackHtmlBody = buildFallEmailHtml(dto);
            emailService.sendSimpleEmail(
                    dto.getEmail(),
                    "Кредитный договор №" + statementId,
                    fallbackHtmlBody
            );
            log.warn("PDF не был сгенерирован для заявки {}, отправлено письмо без вложения", statementId);
        }
    }

    private String buildEmailHtml(DealDocumentDto data) {
        return String.format("""
                <h2>Уважаемый %s %s!</h2>
                <p>Ваш кредитный договор №%s готов.</p>
                <p>Документ прикреплён к письму.</p>
                <p>С уважением,<br>Ваш банк</p>
                """, data.getFirstName(), data.getLastName(), data.getStatementId());
    }

    private String buildFallEmailHtml(DealDocumentDto data) {
        return String.format("""
                <h2>Уважаемый %s %s!</h2>
                <p>Возникли технические сложности с составлением договора по заявке №%s.</p>
                <p>Обратитесь в техническую поддержку.</p>
                <p>С уважением,<br>Ваш банк</p>
                """, data.getFirstName(), data.getLastName(), data.getStatementId());
    }
}