package org.neoflex.deal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.neoflex.deal.model.enums.Gender;
import org.neoflex.deal.model.enums.MaritalStatus;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@Schema(description = "Завершения регистрации клиента")
public class FinishRegistrationRequestDto {

    @NotNull(message = "Пол обязателен")
    @Schema(description = "Пол", example = "MALE")
    private Gender gender;

    @NotNull(message = "Семейное положение обязательно")
    @Schema(description = "Семейное положение", example = "MARRIED")
    private MaritalStatus maritalStatus;

    @NotNull(message = "Количество иждивенцев обязательно")
    @Min(value = 0, message = "Количество иждивенцев не может быть отрицательным")
    @Max(value = 20, message = "Количество иждивенцев не должно превышать 20")
    private Integer dependentAmount;

    @NotNull(message = "Дата выдачи паспорта обязательна")
    @PastOrPresent(message = "Дата выдачи паспорта не может быть в будущем")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата выдачи паспорта", example = "2010-05-15")
    private LocalDate passportIssueDate;

    @NotBlank(message = "Код подразделения обязателен")
    @Pattern(regexp = "^\\d{3}-\\d{3}$", message = "Код подразделения должен быть в формате 123-456")
    @Schema(description = "Код подразделения", example = "123-456")
    private String passportIssueBranch;

    @Valid
    @NotNull(message = "Информация о занятости обязательна")
    @Schema(description = "Информация о занятости")
    private EmploymentDto employment;

    @NotBlank(message = "Номер счета обязателен")
    @Pattern(regexp = "^\\d{20}$", message = "Номер счета должен содержать 20 цифр")
    @Schema(description = "Номер счета", example = "40817810000000000001")
    private String accountNumber;
}
