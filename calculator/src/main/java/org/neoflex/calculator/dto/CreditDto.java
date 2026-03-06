package org.neoflex.calculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "Кредит")
public class CreditDto {

    @Schema(description = "Сумма кредита", example = "1000000")
    private BigDecimal amount;

    @Schema(description = "Срок кредита в месяцах", example = "12")
    private Integer term;

    @Schema(description = "Ежемесячный платеж", example = "87500")
    private BigDecimal monthlyPayment;

    @Schema(description = "Процентная ставка", example = "12.5")
    private BigDecimal rate;

    @Schema(description = "Полная стоимость кредита", example = "15.3")
    private BigDecimal psk;

    @Schema(description = "Флаг страховки", example = "true")
    private Boolean isInsuranceEnabled;

    @Schema(description = "Зарплатный клиент", example = "false")
    private Boolean isSalaryClient;

    @Schema(description = "График платежей")
    private List<PaymentScheduleElementDto> paymentSchedule;
}
