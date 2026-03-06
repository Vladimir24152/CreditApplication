package org.neoflex.calculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@Schema(description = "Запрос на кредит")
public class LoanStatementRequestDto {

    @NotNull(message = "Сумма кредита обязательна")
    @DecimalMin(value = "20000.00", message = "Сумма кредита должна быть не менее 20000")
    @Schema(description = "Сумма кредита", example = "1000000")
    private BigDecimal amount;

    @NotNull(message = "Срок кредита обязателен")
    @Min(value = 6, message = "Срок кредита должен быть не менее 6 месяцев")
    @Schema(description = "Срок кредита в месяцах", example = "12")
    private Integer term;

    @NotBlank(message = "Имя обязательно")
    @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Имя должно содержать только латинские буквы от 2 до 30 символов")
    @Schema(description = "Имя", example = "Ivan")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Фамилия должна содержать только латинские буквы от 2 до 30 символов")
    @Schema(description = "Фамилия", example = "Ivanov")
    private String lastName;

    @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Отчество должно содержать только латинские буквы от 2 до 30 символов")
    @Schema(description = "Отчество", example = "Ivanovich")
    private String middleName;

    @NotBlank(message = "Email обязателен")
    @Pattern(regexp = "^[a-z0-9A-Z_!#$%&'*+/=?`{|}~^.-]+@[a-z0-9A-Z.-]+$",
            message = "Неверный формат email")
    @Schema(description = "Email", example = "ivan@example.com")
    private String email;

    @NotBlank(message = "Дата рождения обязательна")
    @Schema(description = "Дата рождения", example = "1990-01-01")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Дата должна быть в формате ГГГГ-ММ-ДД")
    private String birthDate;

    @NotBlank(message = "Серия паспорта обязательна")
    @Pattern(regexp = "^\\d{4}$", message = "Серия паспорта должна содержать ровно 4 цифры")
    @Schema(description = "Серия паспорта", example = "1234")
    private String passportSeries;

    @NotBlank(message = "Номер паспорта обязателен")
    @Pattern(regexp = "^\\d{6}$", message = "Номер паспорта должен содержать ровно 6 цифр")
    @Schema(description = "Номер паспорта", example = "567890")
    private String passportNumber;
}
