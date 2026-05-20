package org.neoflex.deal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "Сумма кредита обязательна")
    @DecimalMin(value = "20000.00", message = "Сумма кредита должна быть не менее 20000")
    @DecimalMax(value = "10000000.00", message = "Сумма кредита не может превышать 10 000 000")
    @Schema(description = "Сумма кредита", example = "1000000")
    private BigDecimal amount;

    @NotNull(message = "Срок кредита обязателен")
    @Min(value = 6, message = "Срок кредита должен быть не менее 6 месяцев")
    @Max(value = 360, message = "Срок кредита не может превышать 360 месяцев (30 лет)")
    @Schema(description = "Срок кредита в месяцах", example = "12")
    private Integer term;

    @NotNull(message = "Ежемесячный платеж обязателен")
    @DecimalMin(value = "0.01", message = "Ежемесячный платеж должен быть положительным")
    @Schema(description = "Ежемесячный платеж", example = "87500")
    private BigDecimal monthlyPayment;

    @NotNull(message = "Процентная ставка обязательна")
    @DecimalMin(value = "0.1", message = "Процентная ставка должна быть не менее 0.1%")
    @Schema(description = "Процентная ставка", example = "12.5")
    private BigDecimal rate;

    @NotNull(message = "Полная стоимость кредита обязательна")
    @DecimalMin(value = "0.01", message = "Полная стоимость кредита должна быть положительной")
    @Schema(description = "Полная стоимость кредита", example = "1100000")
    private BigDecimal psk;

    @NotNull(message = "Флаг страховки обязателен")
    @Schema(description = "Флаг страховки", example = "true")
    private Boolean isInsuranceEnabled;

    @NotNull(message = "Флаг зарплатного клиента обязателен")
    @Schema(description = "Зарплатный клиент", example = "false")
    private Boolean isSalaryClient;

    @NotNull(message = "График платежей обязателен")
    @Size(min = 1, message = "График платежей не может быть пустым")
    @Schema(description = "График платежей")
    private List<PaymentScheduleElementDto> paymentSchedule;
}
