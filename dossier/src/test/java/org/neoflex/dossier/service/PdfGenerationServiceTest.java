package org.neoflex.dossier.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neoflex.dossier.dto.DealDocumentDto;
import org.neoflex.dossier.dto.PaymentScheduleElementDto;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Тесты сервиса PdfGenerationService")
@ExtendWith(MockitoExtension.class)
class PdfGenerationServiceTest {

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private PdfGenerationService pdfGenerationService;

    private DealDocumentDto dealDocumentDto;
    private UUID statementId;

    @BeforeEach
    void setUp() {
        statementId = UUID.randomUUID();

        List<PaymentScheduleElementDto> paymentSchedule = List.of(
                PaymentScheduleElementDto.builder()
                        .number(1)
                        .date(LocalDate.now().plusMonths(1))
                        .totalPayment(BigDecimal.valueOf(50000))
                        .principalPayment(BigDecimal.valueOf(40000))
                        .interestPayment(BigDecimal.valueOf(10000))
                        .remainingDebt(BigDecimal.valueOf(960000))
                        .build()
        );

        dealDocumentDto = DealDocumentDto.builder()
                .statementId(statementId)
                .signDate(LocalDate.now())
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("ivan@example.com")
                .accountNumber("40817810000000000001")
                .passportSeries("1234")
                .passportNumber("567890")
                .passportIssueDate(LocalDate.of(2010, 5, 15))
                .passportIssueBranch("123-456")
                .amount(BigDecimal.valueOf(1000000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(87451))
                .rate(BigDecimal.valueOf(9))
                .psk(BigDecimal.valueOf(1049417))
                .isInsuranceEnabled(true)
                .isSalaryClient(true)
                .paymentSchedule(paymentSchedule)
                .build();
    }

    @Test
    @DisplayName("Проверка вызова TemplateEngine для генерации HTML")
    void generateCreditAgreementShouldCallTemplateEngine() {
        String expectedHtml = "<html>Test</html>";
        when(templateEngine.process(eq("credit_agreement"), any(Context.class))).thenReturn(expectedHtml);

        pdfGenerationService.generateCreditAgreement(dealDocumentDto);

        verify(templateEngine).process(eq("credit_agreement"), any(Context.class));
    }
}