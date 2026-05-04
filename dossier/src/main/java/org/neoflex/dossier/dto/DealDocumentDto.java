package org.neoflex.dossier.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.neoflex.dossier.enums.Gender;
import org.neoflex.dossier.enums.MaritalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(description = "Данные для генерации кредитного договора")
public class DealDocumentDto {

    @NotNull(message = "Идентификатор предложения обязателен")
    @Schema(description = "Идентификатор предложения", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID statementId;

    @NotNull(message = "Дата подписания договора")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата подписания договора", example = "2024-12-15")
    private LocalDate signDate;

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

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата рождения", example = "1990-01-01")
    private LocalDate birthDate;

    @NotNull(message = "Email обязателен")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Неверный формат email")
    @Schema(description = "Email клиента", example = "ivan@example.com")
    private String email;

    @NotNull(message = "Пол обязателен")
    @Schema(description = "Пол", example = "MALE")
    private Gender gender;

    @NotNull(message = "Семейное положение обязательно")
    @Schema(description = "Семейное положение", example = "MARRIED")
    private MaritalStatus maritalStatus;

    @NotNull(message = "Количество иждивенцев обязательно")
    @Min(value = 0, message = "Количество иждивенцев не может быть отрицательным")
    @Max(value = 10, message = "Количество иждивенцев не должно превышать 10")
    private Integer dependentAmount;

    @NotBlank(message = "Номер счета обязателен")
    @Pattern(regexp = "^\\d{20}$", message = "Номер счета должен содержать 20 цифр")
    @Schema(description = "Номер счета", example = "40817810000000000001")
    private String accountNumber;

    @NotBlank(message = "Серия паспорта обязательна")
    @Pattern(regexp = "^\\d{4}$", message = "Серия паспорта должна содержать ровно 4 цифры")
    @Schema(description = "Серия паспорта", example = "1234")
    private String passportSeries;

    @NotBlank(message = "Номер паспорта обязателен")
    @Pattern(regexp = "^\\d{6}$", message = "Номер паспорта должен содержать ровно 6 цифр")
    @Schema(description = "Номер паспорта", example = "567890")
    private String passportNumber;

    @NotNull(message = "Дата выдачи паспорта обязательна")
    @PastOrPresent(message = "Дата выдачи паспорта не может быть в будущем")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата выдачи паспорта", example = "2010-05-15")
    private LocalDate passportIssueDate;

    @NotBlank(message = "Код подразделения обязателен")
    @Pattern(regexp = "^\\d{3}-\\d{3}$", message = "Код подразделения должен быть в формате 123-456")
    @Schema(description = "Код подразделения", example = "123-456")
    private String passportIssueBranch;

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
