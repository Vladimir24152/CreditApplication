package org.neoflex.deal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.neoflex.deal.model.enums.ApplicationStatus;
import org.neoflex.deal.model.jsonb.StatusHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(description = "Ответ с данными заявки")
public class StatementResponseDto {

    @NotNull(message = "Идентификатор заявки не может быть null")
    @Schema(description = "Идентификатор заявки", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID statementId;

    @Valid
    @NotNull(message = "Данные клиента обязательны")
    @Schema(description = "Клиент")
    private ClientDto client;

    @Valid
    @Schema(description = "Кредит (может отсутствовать, если заявка не одобрена)")
    private CreditDto credit;

    @NotNull(message = "Статус заявки обязателен")
    @Schema(description = "Текущий статус заявки")
    private ApplicationStatus status;

    @NotNull(message = "Дата создания обязательна")
    @PastOrPresent(message = "Дата создания не может быть в будущем")
    @Schema(description = "Дата создания заявки", example = "2026-05-18T12:00:00")
    private LocalDateTime creationDate;

    @Valid
    @Schema(description = "Выбранное кредитное предложение")
    private LoanOfferDto appliedOffer;

    @PastOrPresent(message = "Дата подписания не может быть в будущем")
    @Schema(description = "Дата подписания договора")
    private LocalDateTime signDate;

    @Valid
    @NotNull(message = "История статусов не может быть null")
    @Schema(description = "История изменения статусов заявки")
    private List<StatusHistory> statusHistory;
}
