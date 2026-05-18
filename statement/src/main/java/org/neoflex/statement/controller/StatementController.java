package org.neoflex.statement.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.neoflex.credit.lib.exception.HttpErrorInternalServiceResponse;
import org.neoflex.statement.dto.LoanOfferDto;
import org.neoflex.statement.dto.LoanStatementRequestDto;
import org.neoflex.statement.service.StatementService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/statement")
@RequiredArgsConstructor
@Tag(name = "Statement Management", description = "API для управления кредитными заявками")
public class StatementController {

    private final StatementService statementService;

    @Operation(summary = "Расчет возможных условий кредита",
            description = "Принимает заявку на кредит и возвращает список кредитных предложений")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Кредитные предложения успешно сформированы",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = LoanOfferDto.class))
                    )
            ),
            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации входных данных (некорректная дата рождения, паспортные данные)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера при обработке запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "502",
                    description = "получает некорректный ответ от стороннего сервиса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            )
    })
    @PostMapping
    public List<LoanOfferDto> preScoringAndGetOffers(@Valid @RequestBody LoanStatementRequestDto request) {
        return statementService.getOffers(request);
    }

    @Operation(summary = "Выбор одного из кредитных предложений",
            description = "Выбирает конкретное кредитное предложение для дальнейшего оформления")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Выбор кредитного предложения успешно сохранен"
            ),
            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации входных данных (отрицательное значение суммы займа и др.)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Не найдена заявка с id указанным в запросе",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера при обработке запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "502",
                    description = "получает некорректный ответ от стороннего сервиса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            )
    })
    @PostMapping("/offer")
    void selectOffer(@Valid @RequestBody LoanOfferDto request) {
        statementService.selectOffer(request);
    }

}
