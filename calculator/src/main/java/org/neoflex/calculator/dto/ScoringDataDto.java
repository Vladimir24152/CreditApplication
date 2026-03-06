package org.neoflex.calculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@Schema(description = "Данные для скоринга")
public class ScoringDataDto {

    @Schema(description = "Сумма кредита", example = "1000000")
    private BigDecimal amount;

    @Schema(description = "Срок кредита", example = "12")
    private Integer term;

    @Schema(description = "Имя", example = "Иван")
    private String firstName;

    @Schema(description = "Фамилия", example = "Иванов")
    private String lastName;

    @Schema(description = "Отчество", example = "Иванович")
    private String middleName;

    @Schema(description = "Пол", example = "MALE")
    private String gender;

    @Schema(description = "Дата рождения", example = "1990-01-01")
    private String birthDate;

    @Schema(description = "Серия паспорта", example = "1234")
    private String passportSeries;

    @Schema(description = "Номер паспорта", example = "567890")
    private String passportNumber;

    @Schema(description = "Дата выдачи паспорта", example = "2010-05-15")
    private String passportIssueDate;

    @Schema(description = "Код подразделения", example = "123-456")
    private String passportIssueBranch;

    @Schema(description = "Семейное положение", example = "MARRIED")
    private String maritalStatus;

    @Schema(description = "Количество иждивенцев", example = "2")
    private Integer dependentAmount;

    @Schema(description = "Информация о занятости")
    private EmploymentDto employment;

    @Schema(description = "Номер счета", example = "40817810000000000001")
    private String accountNumber;

    @Schema(description = "Флаг страховки", example = "true")
    private Boolean isInsuranceEnabled;

    @Schema(description = "Зарплатный клиент", example = "false")
    private Boolean isSalaryClient;
}
