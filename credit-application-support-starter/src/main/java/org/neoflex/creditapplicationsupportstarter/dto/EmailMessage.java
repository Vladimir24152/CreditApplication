package org.neoflex.creditapplicationsupportstarter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neoflex.creditapplicationsupportstarter.enums.Theme;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Сообщение для отправки на почту через Kafka")
public class EmailMessage {

    @NotBlank(message = "Email адрес не может быть пустым")
    @Email(message = "Неверный формат email адреса")
    @Schema(description = "Email адрес получателя",example = "client@example.com")
    private String address;

    @NotNull(message = "Тема сообщения не может быть null")
    @Schema(description = "Тема сообщения (тип уведомления)",example = "FINISH_REGISTRATION")
    private Theme theme;

    @NotNull(message = "ID заявки не может быть null")
    @Positive(message = "ID заявки должен быть положительным числом")
    @Schema(description = "Идентификатор заявки (Statement ID)",example = "12345")
    private UUID statementId;

    @NotBlank(message = "Текст сообщения не может быть пустым")
    @Schema(description = "Текст сообщения для отправки",
            example = "Ваша заявка успешно зарегистрирована. Пожалуйста, завершите регистрацию.")
    private String text;
}