package org.neoflex.calculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.neoflex.calculator.annotations.MinimumCurrentWorkExperience;
import org.neoflex.calculator.annotations.MinimumTotalWorkExperience;
import org.neoflex.calculator.annotations.NotUnemployed;
import org.neoflex.calculator.enums.EmploymentStatus;
import org.neoflex.calculator.enums.Position;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@Schema(description = "Информация о занятости")
public class EmploymentDto {

    @NotBlank(message = "ИНН работодателя обязателен")
    @Pattern(regexp = "^\\d{10}$|^\\d{12}$", message = "ИНН должен содержать 10 или 12 цифр")
    @Schema(description = "ИНН работодателя", example = "1234567890")
    private String employerInn;

    @NotUnemployed(message = "Отказ в займе нетрудоустроенным клиентам")
    @NotNull(message = "Статус занятости обязателен")
    @Schema(description = "Статус занятости", example = "EMPLOYED")
    private EmploymentStatus employmentStatus;

    @NotNull(message = "Должность обязательна")
    @Schema(description = "Должность", example = "SPECIALIST")
    private Position position;

    @NotNull(message = "Общий стаж обязателен")
    @Min(value = 0, message = "Общий стаж не может быть отрицательным")
    @Max(value = 900, message = "Общий стаж не может превышать 900 месяцев")
    @MinimumTotalWorkExperience(countOfMonth = 18, message = "Отказ в займе клиентам с общим стажем менее 18 месяцев")
    @Schema(description = "Общий стаж в месяцах", example = "60")
    private Integer workExperienceTotal;

    @NotNull(message = "Текущий стаж обязателен")
    @Min(value = 0, message = "Текущий стаж не может быть отрицательным")
    @Max(value = 900, message = "Текущий стаж не может превышать 900 месяцев")
    @MinimumCurrentWorkExperience(countOfMonth = 3, message = "Отказ в займе клиентам с текущим стажем менее 3 месяцев")
    @Schema(description = "Текущий стаж в месяцах", example = "24")
    private Integer workExperienceCurrent;

    @NotNull(message = "Ежемесячный доход обязателен")
    @DecimalMin(value = "0.01", message = "Доход должен быть положительным")
    @Schema(description = "Ежемесячный доход", example = "100000")
    private BigDecimal salary;
}
