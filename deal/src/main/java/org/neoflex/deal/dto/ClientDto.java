package org.neoflex.deal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.neoflex.deal.model.enums.Gender;
import org.neoflex.deal.model.enums.MaritalStatus;
import org.neoflex.deal.model.jsonb.Employment;
import org.neoflex.deal.model.jsonb.Passport;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@Schema(description = "Данные клиента")
public class ClientDto {

    @NotNull(message = "Фамилия обязательна")
    @Size(min = 2, max = 30, message = "Фамилия должна быть от 2 до 30 символов")
    @Schema(description = "Фамилия", example = "Иванов")
    private String lastName;

    @NotNull(message = "Имя обязательно")
    @Size(min = 2, max = 30, message = "Имя должно быть от 2 до 30 символов")
    @Schema(description = "Имя", example = "Иван")
    private String firstName;

    @Size(max = 30, message = "Отчество не более 30 символов")
    @Schema(description = "Отчество", example = "Иванович")
    private String middleName;

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    @Schema(description = "Дата рождения", example = "1990-01-01")
    private LocalDate birthDate;

    @NotNull(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    @Schema(description = "Email", example = "ivan@example.com")
    private String email;

    @Schema(description = "Пол", example = "MALE")
    private Gender gender;

    @Schema(description = "Семейное положение", example = "MARRIED")
    private MaritalStatus maritalStatus;

    @PositiveOrZero(message = "Количество иждивенцев не может быть отрицательным")
    @Schema(description = "Количество иждивенцев", example = "1")
    private Integer dependentAmount;

    @Valid
    @Schema(description = "Паспортные данные")
    private Passport passport;

    @Valid
    @Schema(description = "Данные о трудоустройстве")
    private Employment employment;

    @Schema(description = "Номер счета", example = "40817810099910004312")
    private String accountNumber;
}