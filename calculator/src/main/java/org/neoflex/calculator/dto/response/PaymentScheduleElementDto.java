package org.neoflex.calculator.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@Schema(description = "Элемент графика платежей")
public class PaymentScheduleElementDto {

    @NotNull(message = "Номер платежа обязателен")
    @Min(value = 1, message = "Номер платежа должен быть положительным числом")
    @Schema(description = "Номер платежа", example = "1")
    private Integer number;

    @NotNull(message = "Дата платежа обязательна")
    @FutureOrPresent(message = "Дата платежа не может быть в прошлом")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата платежа", example = "2024-01-15")
    private LocalDate date;

    @NotNull(message = "Сумма платежа обязательна")
    @DecimalMin(value = "0.01", message = "Сумма платежа должна быть больше 0")
    @Positive(message = "Сумма платежа должна быть положительной")
    @Schema(description = "Сумма платежа", example = "87500")
    private BigDecimal totalPayment;

    @NotNull(message = "Платеж по основному долгу обязателен")
    @DecimalMin(value = "0.00", message = "Платеж по основному долгу не может быть отрицательным")
    @Schema(description = "Платеж по основному долгу", example = "82500")
    private BigDecimal principalPayment;

    @NotNull(message = "Платеж по процентам обязателен")
    @DecimalMin(value = "0.00", message = "Платеж по процентам не может быть отрицательным")
    @Schema(description = "Платеж по процентам", example = "5000")
    private BigDecimal interestPayment;

    @NotNull(message = "Остаток долга обязателен")
    @DecimalMin(value = "0.00", message = "Остаток долга не может быть отрицательным")
    @Schema(description = "Остаток долга", example = "917500")
    private BigDecimal remainingDebt;
}
