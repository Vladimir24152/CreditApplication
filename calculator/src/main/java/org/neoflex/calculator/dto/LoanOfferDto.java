package org.neoflex.calculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(description = "Кредитное предложение")
public class LoanOfferDto {

    @Schema(description = "Идентификатор предложения", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID statementId;

    @Schema(description = "Запрошенная сумма", example = "1000000")
    private BigDecimal requestedAmount;

    @Schema(description = "Итоговая сумма", example = "1050000")
    private BigDecimal totalAmount;

    @Schema(description = "Срок кредита в месяцах", example = "12")
    private Integer term;

    @Schema(description = "Ежемесячный платеж", example = "87500")
    private BigDecimal monthlyPayment;

    @Schema(description = "Процентная ставка", example = "12.5")
    private BigDecimal rate;

    @Schema(description = "Флаг страховки", example = "true")
    private Boolean isInsuranceEnabled;

    @Schema(description = "Зарплатный клиент", example = "false")
    private Boolean isSalaryClient;
}
