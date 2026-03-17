package org.neoflex.calculator.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Идентификатор предложения обязателен")
    @Schema(description = "Идентификатор предложения", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID statementId;

    @NotNull(message = "Запрошенная сумма обязательна")
    @DecimalMin(value = "20000.00", message = "Запрошенная сумма должна быть не менее 20000")
    @Schema(description = "Запрошенная сумма", example = "1000000")
    private BigDecimal requestedAmount;

    @NotNull(message = "Итоговая сумма обязательна")
    @DecimalMin(value = "0.01", message = "Итоговая сумма должна быть положительной")
    @Schema(description = "Итоговая сумма", example = "1050000")
    private BigDecimal totalAmount;

    @NotNull(message = "Срок кредита обязателен")
    @Min(value = 6, message = "Срок кредита должен быть не менее 6 месяцев")
    @Schema(description = "Срок кредита в месяцах", example = "12")
    private Integer term;

    @NotNull(message = "Ежемесячный платеж обязателен")
    @DecimalMin(value = "0.01", message = "Ежемесячный платеж должен быть положительным")
    @Schema(description = "Ежемесячный платеж", example = "87500")
    private BigDecimal monthlyPayment;

    @NotNull(message = "Процентная ставка обязательна")
    @DecimalMin(value = "0.1", message = "Процентная ставка должна быть не менее 0.1%")
    @DecimalMax(value = "99.9", message = "Процентная ставка не может превышать 99.9%")
    @Schema(description = "Процентная ставка", example = "12.5")
    private BigDecimal rate;

    @NotNull(message = "Флаг страховки обязателен")
    @Schema(description = "Флаг страховки", example = "true")
    private Boolean isInsuranceEnabled;

    @NotNull(message = "Флаг зарплатного клиента обязателен")
    @Schema(description = "Зарплатный клиент", example = "false")
    private Boolean isSalaryClient;
}
