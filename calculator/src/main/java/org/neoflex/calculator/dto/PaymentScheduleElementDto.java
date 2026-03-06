package org.neoflex.calculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Номер платежа", example = "1")
    private Integer number;

    @Schema(description = "Дата платежа", example = "2024-01-15")
    private LocalDate date;

    @Schema(description = "Сумма платежа", example = "87500")
    private BigDecimal totalPayment;

    @Schema(description = "Платеж по основному долгу", example = "82500")
    private BigDecimal principalPayment;

    @Schema(description = "Платеж по процентам", example = "5000")
    private BigDecimal interestPayment;

    @Schema(description = "Остаток долга", example = "917500")
    private BigDecimal remainingDebt;
}
