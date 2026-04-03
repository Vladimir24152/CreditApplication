package org.neoflex.deal.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.neoflex.deal.dto.CreditDto;
import org.neoflex.deal.dto.PaymentScheduleElementDto;
import org.neoflex.deal.model.Credit;
import org.neoflex.deal.model.enums.CreditStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Тесты маппера CreditMapper")
class CreditMapperTest {

    private CreditMapper creditMapper;

    private CreditDto creditDto;

    @BeforeEach
    void setUp() {
        creditMapper = Mappers.getMapper(CreditMapper.class);

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

        creditDto = CreditDto.builder()
                .amount(BigDecimal.valueOf(1000000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(50000))
                .rate(BigDecimal.valueOf(12))
                .psk(BigDecimal.valueOf(600000))
                .isInsuranceEnabled(true)
                .isSalaryClient(true)
                .paymentSchedule(paymentSchedule)
                .build();
    }

    @Test
    @DisplayName("Маппинг CreditDto и статуса в Credit должен создавать кредит со всеми полями")
    void toCreditShouldMapAllFieldsCorrectly() {
        CreditStatus status = CreditStatus.CALCULATED;
        Credit result = creditMapper.toCredit(creditDto, status);

        assertNotNull(result);

        assertEquals(status, result.getCreditStatus());

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("creditId", "creditStatus")
                .isEqualTo(creditDto);
    }

    @Test
    @DisplayName("Маппинг CreditDto и статуса должен устанавливать переданный статус кредита")
    void toCreditShouldSetProvidedCreditStatus() {
        CreditStatus status = CreditStatus.ISSUED;
        Credit result = creditMapper.toCredit(creditDto, status);

        assertEquals(CreditStatus.ISSUED, result.getCreditStatus());
    }

    @Test
    @DisplayName("Маппинг CreditDto должен корректно маппить график платежей")
    void toCreditShouldMapPaymentScheduleCorrectly() {
        Credit result = creditMapper.toCredit(creditDto, CreditStatus.CALCULATED);

        assertNotNull(result.getPaymentSchedule());
        assertEquals(1, result.getPaymentSchedule().size());

        PaymentScheduleElementDto firstElementResult = result.getPaymentSchedule().getFirst();
        PaymentScheduleElementDto firstElementRequest = creditDto.getPaymentSchedule().getFirst();

        assertThat(firstElementResult)
                .usingRecursiveComparison()
                .isEqualTo(firstElementRequest);
    }
}