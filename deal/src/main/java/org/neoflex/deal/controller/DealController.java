package org.neoflex.deal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.neoflex.deal.dto.FinishRegistrationRequestDto;
import org.neoflex.deal.dto.LoanOfferDto;
import org.neoflex.deal.dto.LoanStatementRequestDto;
import org.neoflex.deal.dto.response.HttpErrorResponse;
import org.neoflex.deal.service.DealService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deal")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    @Operation(summary = "Расчет возможных условий кредита",
            description = "Принимает заявку на кредит и возвращает список кредитных предложений")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Кредитные предложения успешно сформированы",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoanOfferDto.class)
                    )
            ),
            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации входных данных (некорректная дата рождения, паспортные данные)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "validationErrorExample",
                                    summary = "Ответ ошибки валидации",
                                    value = """
                                            {
                                               "code": 400,
                                               "type": "Bad Request",
                                               "timestamp": "2026-03-16T23:52:18.9894373",
                                               "message": "Поле 'firstName': Имя должно содержать только латинские буквы от 2 до 30 символов"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера при обработке запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "internalErrorExample",
                                    summary = "Пример пример внутренней ошибки",
                                    value = """
                                            {
                                               "code": 500,
                                               "type": "Internal Server Error",
                                               "timestamp": "2024-01-15T14:30:25.123",
                                               "message": "Ошибка сервера"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/statement")
    public List<LoanOfferDto> calculationOfPossibleLoanTerms(@Valid @RequestBody LoanStatementRequestDto request){
        return dealService.calculationOfPossibleLoanTerms(request);
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
                            schema = @Schema(implementation = HttpErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "validationErrorExample",
                                    summary = "Ответ ошибки валидации",
                                    value = """
                                            {
                                               "code": 400,
                                               "type": "Bad Request",
                                               "timestamp": "2026-03-16T23:52:18.9894373",
                                               "message": "Поле 'requestedAmount': Запрошенная сумма должна быть не менее 20000"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Не найдена заявка с id указанным в запросе",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "entityNotFoundErrorExample",
                                    summary = "Ответ ошибки отсутствия сущности",
                                    value = """
                                            {
                                               "code": 404,
                                               "type": "ENTITY_NOT_FOUND",
                                               "timestamp": "2026-03-31T13:03:13.586830362",
                                               "message": "Не найдена заявка с id указанным в запросе: 628ded27-6eb1-4bb2-8240-9edbea2d33f6"
                                             }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера при обработке запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "internalErrorExample",
                                    summary = "Пример пример внутренней ошибки",
                                    value = """
                                            {
                                               "code": 500,
                                               "type": "Internal Server Error",
                                               "timestamp": "2024-01-15T14:30:25.123",
                                               "message": "Ошибка сервера"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/offer/select")
    public void choosingOneOfTheLoanOffers(@Valid @RequestBody LoanOfferDto request){
        dealService.choosingOneOfTheLoanOffers(request);
    }

    @Operation(summary = "Завершение регистрации и полный расчет кредита",
            description = "Завершает регистрацию клиента и выполняет полный расчет кредита")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное завершение регистрации и расчета кредита"
            ),
            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации входных данных (код подразделения в неверном формате и др.)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "validationErrorExample",
                                    summary = "Ответ ошибки валидации",
                                    value = """
                                            {
                                               "code": 400,
                                               "type": "Bad Request",
                                               "timestamp": "2026-03-16T23:52:18.9894373",
                                               "message": "Поле 'passportIssueBranch': Код подразделения должен быть в формате 123-456"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Не найдена заявка с id указанным в запросе",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "entityNotFoundErrorExample",
                                    summary = "Ответ ошибки отсутствия сущности",
                                    value = """
                                            {
                                               "code": 404,
                                               "type": "ENTITY_NOT_FOUND",
                                               "timestamp": "2026-03-31T13:03:13.586830362",
                                               "message": "Не найдена заявка с id указанным в запросе: 628ded27-6eb1-4bb2-8240-9edbea2d33f6"
                                             }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера при обработке запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "internalErrorExample",
                                    summary = "Пример пример внутренней ошибки",
                                    value = """
                                            {
                                               "code": 500,
                                               "type": "Internal Server Error",
                                               "timestamp": "2024-01-15T14:30:25.123",
                                               "message": "Ошибка сервера"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/calculate/{statementId}")
    public void completionOfRegistrationAndFullCreditCalculation(
            @Valid @RequestBody FinishRegistrationRequestDto request,
            @RequestParam String statementId
    ){
        dealService.completionOfRegistrationAndFullCreditCalculation(request,statementId);
    }
}
