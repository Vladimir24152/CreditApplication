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
        context.setVariable("agreement", dto);

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
            log.debug("Сгенерирован PDF кредитного договора {}", dto.getStatementId());
            return Optional.of(baos.toByteArray());
        } catch (IOException e) {
            log.error("Ошибка генерации PDF кредитного договора", e);
            return Optional.empty();
        }
    }
}
