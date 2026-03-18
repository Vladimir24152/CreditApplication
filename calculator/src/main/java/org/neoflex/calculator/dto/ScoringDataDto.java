package org.neoflex.calculator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.neoflex.calculator.annotations.Adult;
import org.neoflex.calculator.annotations.AgeRange;
import org.neoflex.calculator.annotations.VerificationAmountVsSalary;
import org.neoflex.calculator.enums.Gender;
import org.neoflex.calculator.enums.MaritalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@VerificationAmountVsSalary
@Schema(description = "Данные для скоринга")
public class ScoringDataDto {

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

    @NotNull(message = "Пол обязателен")
    @Schema(description = "Пол", example = "MALE")
    private Gender gender;

    @Adult(message = "Клиент должен быть совершеннолетним")
    @AgeRange(minAge = 20,maxAge = 65, message = "Отказ в займе для клиентов младше 20 и старше 65 лет")
    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата рождения", example = "1990-01-01")
    private LocalDate birthDate;

    @NotBlank(message = "Серия паспорта обязательна")
    @Pattern(regexp = "^\\d{4}$", message = "Серия паспорта должна содержать ровно 4 цифры")
    @Schema(description = "Серия паспорта", example = "1234")
    private String passportSeries;

    @NotBlank(message = "Номер паспорта обязателен")
    @Pattern(regexp = "^\\d{6}$", message = "Номер паспорта должен содержать ровно 6 цифр")
    @Schema(description = "Номер паспорта", example = "567890")
    private String passportNumber;

    @NotNull(message = "Дата выдачи паспорта обязательна")
    @Past(message = "Дата выдачи паспорта не может быть в будущем")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата выдачи паспорта", example = "2010-05-15")
    private LocalDate passportIssueDate;

    @NotBlank(message = "Код подразделения обязателен")
    @Pattern(regexp = "^\\d{3}-\\d{3}$", message = "Код подразделения должен быть в формате 123-456")
    @Schema(description = "Код подразделения", example = "123-456")
    private String passportIssueBranch;

    @NotNull(message = "Семейное положение обязательно")
    @Schema(description = "Семейное положение", example = "MARRIED")
    private MaritalStatus maritalStatus;

    @Min(value = 0, message = "Количество иждивенцев не может быть отрицательным")
    @Max(value = 10, message = "Количество иждивенцев не может превышать 10")
    @Schema(description = "Количество иждивенцев", example = "2")
    private Integer dependentAmount;

    @NotNull(message = "Информация о занятости обязательна")
    @Valid
    @Schema(description = "Информация о занятости")
    private EmploymentDto employment;

    @NotBlank(message = "Номер счета обязателен")
    @Pattern(regexp = "^\\d{20}$", message = "Номер счета должен содержать 20 цифр")
    @Schema(description = "Номер счета", example = "40817810000000000001")
    private String accountNumber;

    @NotNull(message = "Флаг страховки обязателен")
    @Schema(description = "Флаг страховки", example = "true")
    private Boolean isInsuranceEnabled;

    @NotNull(message = "Флаг зарплатного клиента обязателен")
    @Schema(description = "Зарплатный клиент", example = "false")
    private Boolean isSalaryClient;
}
