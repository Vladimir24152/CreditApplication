package org.neoflex.calculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@Schema(description = "Информация о занятости")
public class EmploymentDto {

    @Schema(description = "ИНН работодателя", example = "1234567890")
    private String employerInn;

    @Schema(description = "Статус занятости", example = "EMPLOYED")
    private String employmentStatus;

    @Schema(description = "Должность", example = "ENGINEER")
    private String position;

    @Schema(description = "Общий стаж", example = "60")
    private Integer workExperienceTotal;

    @Schema(description = "Текущий стаж", example = "24")
    private Integer workExperienceCurrent;

    @Schema(description = "Ежемесячный доход", example = "100000")
    private BigDecimal salary;
}
