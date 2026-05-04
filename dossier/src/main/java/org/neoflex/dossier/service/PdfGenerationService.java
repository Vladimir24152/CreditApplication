package org.neoflex.dossier.service;

import com.lowagie.text.pdf.BaseFont;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.dossier.dto.DealDocumentDto;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    private final TemplateEngine templateEngine;

    public Optional<byte[]> generateCreditAgreement(DealDocumentDto dto) {
        Context context = new Context();
        context.setVariable("statementId", dto.getStatementId());
        context.setVariable("signDate", dto.getSignDate());
        context.setVariable("firstName", dto.getFirstName());
        context.setVariable("lastName", dto.getLastName());
        context.setVariable("middleName", dto.getMiddleName());
        context.setVariable("birthDate", dto.getBirthDate());
        context.setVariable("email", dto.getEmail());
        context.setVariable("accountNumber", dto.getAccountNumber());
        context.setVariable("passportSeries", dto.getPassportSeries());
        context.setVariable("passportNumber", dto.getPassportNumber());
        context.setVariable("passportIssueDate", dto.getPassportIssueDate());
        context.setVariable("passportIssueBranch", dto.getPassportIssueBranch());
        context.setVariable("amount", dto.getAmount());
        context.setVariable("term", dto.getTerm());
        context.setVariable("rate", dto.getRate());
        context.setVariable("monthlyPayment", dto.getMonthlyPayment());
        context.setVariable("psk", dto.getPsk());
        context.setVariable("isInsuranceEnabled", dto.getIsInsuranceEnabled());
        context.setVariable("isSalaryClient", dto.getIsSalaryClient());
        context.setVariable("paymentSchedule", dto.getPaymentSchedule());

        log.info("Заполнение контекста завершено для {}", dto.getStatementId());

        String html = templateEngine.process("credit_agreement", context);

        log.info("HTML шаблон обработан, длина HTML: {} символов", html.length());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            renderer.getFontResolver().addFont(
                    "classpath:/fonts/DejaVuSans.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );

            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(baos);
            log.info("Сгенерирован PDF кредитного договора {}", dto.getStatementId());
            return Optional.of(baos.toByteArray());
        } catch (IOException e) {
            log.error("Ошибка генерации PDF кредитного договора", e);
            return Optional.empty();
        }
    }
}
