package org.neoflex.calculator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.neoflex.calculator.dto.LoanStatementRequestDto;
import org.neoflex.calculator.dto.ScoringDataDto;
import org.neoflex.calculator.dto.response.CreditDto;
import org.neoflex.calculator.dto.response.LoanOfferDto;
import org.neoflex.calculator.service.CreditCalculationService;
import org.neoflex.calculator.service.LoanOfferService;
import org.neoflex.credit.lib.exception.HttpErrorInternalServiceResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calculator")
public class CalculatorController {

    private final LoanOfferService loanOfferService;

    private final CreditCalculationService creditCalculationService;

    @Operation(description = "Расчёт возможных условий кредита")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Кредитные предложения успешно сформированы",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoanOfferDto.class),
                            examples = @ExampleObject(
                                    name = "successOffersResponse",
                                    summary = "Успешный ответ со списком предложений",
                                    value = """
                                            [
                                              {
                                                "statementId": "531a472b-6623-4127-a5cd-6f0e0fb34aaa",
                                                "requestedAmount": 1000000,
                                                "totalAmount": 1083099.72,
                                                "term": 12,
                                                "monthlyPayment": 90258.31,
                                                "rate": 15,
                                                "isInsuranceEnabled": false,
                                                "isSalaryClient": false
                                              },
                                              {
                                                "statementId": "44768155-8478-4fed-b2ec-92ee78e58bbf",
                                                "requestedAmount": 1000000,
                                                "totalAmount": 1077445.44,
                                                "term": 12,
                                                "monthlyPayment": 89787.12,
                                                "rate": 14,
                                                "isInsuranceEnabled": false,
                                                "isSalaryClient": true
                                              },
                                              {
                                                "statementId": "636c3358-271d-4833-abe7-f5cf6cef16b6",
                                                "requestedAmount": 1000000,
                                                "totalAmount": 1086185.48,
                                                "term": 12,
                                                "monthlyPayment": 90515.46,
                                                "rate": 12,
                                                "isInsuranceEnabled": true,
                                                "isSalaryClient": false
                                              },
                                              {
                                                "statementId": "f10d6e2a-8456-45bf-a9e1-fd00e6ded352",
                                                "requestedAmount": 1000000,
                                                "totalAmount": 1080579.92,
                                                "term": 12,
                                                "monthlyPayment": 90048.33,
                                                "rate": 11,
                                                "isInsuranceEnabled": true,
                                                "isSalaryClient": true
                                              }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации входных данных (неверный формат email, паспорта и т.д.)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class),
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
            @ApiResponse(
                    responseCode = "422",
                    description = "Ошибка скоринга - клиент не проходит по условиям (возраст, стаж, доход)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class),
                            examples = @ExampleObject(
                                    name = "scoringErrorExample",
                                    summary = "Пример ошибки скоринга",
                                    value = """
                                            {
                                                "code": 422,
                                                 "type": "Unprocessable Entity",
                                                 "timestamp": "2026-03-16T23:53:01.2758672",
                                                 "message": "Поле 'birthDate': Отказ в займе - Неверная дата рождения, Клиент должен быть совершеннолетним"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class),
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
            ),
    })
    @PostMapping("/offers")
    public List<LoanOfferDto> calculateLoanOffers(@Valid @RequestBody LoanStatementRequestDto request){
        return loanOfferService.calculateLoanOffers(request);
    }

    @Operation(description = "Валидация присланных данных, скоринг данных, полный расчет параметров кредита")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Кредит успешно рассчитан, возвращен график платежей",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreditDto.class),
                            examples = @ExampleObject(
                                    name = "successCreditResponse",
                                    summary = "Успешный расчет кредита",
                                    value = """
                                            {
                                               "amount": 1000000,
                                               "term": 12,
                                               "monthlyPayment": 89118.15,
                                               "rate": 9,
                                               "psk": 1069417.76,
                                               "isInsuranceEnabled": true,
                                               "isSalaryClient": false,
                                               "paymentSchedule": [
                                                 {
                                                   "number": 1,
                                                   "date": "2026-04-17",
                                                   "totalPayment": 89118.15,
                                                   "principalPayment": 81618.15,
                                                   "interestPayment": 7500,
                                                   "remainingDebt": 918381.85
                                                 },
                                                 {
                                                   "number": 2,
                                                   "date": "2026-05-17",
                                                   "totalPayment": 89118.15,
                                                   "principalPayment": 82230.29,
                                                   "interestPayment": 6887.86,
                                                   "remainingDebt": 836151.57
                                                 },
                                                 {
                                                   "number": 3,
                                                   "date": "2026-06-17",
                                                   "totalPayment": 89118.15,
                                                   "principalPayment": 82847.01,
                                                   "interestPayment": 6271.14,
                                                   "remainingDebt": 753304.56
                                                 },
                                                 {
                                                   "number": 4,
                                                   "date": "2026-07-17",
                                                   "totalPayment": 89118.15,
                                                   "principalPayment": 83468.37,
                                                   "interestPayment": 5649.78,
                                                   "remainingDebt": 669836.19
                                                 },
                                                 {
                                                   "number": 5,
                                                   "date": "2026-08-17",
                                                   "totalPayment": 89118.15,
                                                   "principalPayment": 84094.38,
                                                   "interestPayment": 5023.77,
                                                   "remainingDebt": 585741.82
                                                 },
                                                 {
                                                   "number": 6,
                                                   "date": "2026-09-17",
                                                   "totalPayment": 89118.15,
                                                   "principalPayment": 84725.09,
                                                   "interestPayment": 4393.06,
                                                   "remainingDebt": 501016.73
                                                 },
                                                 {
                                                   "number": 7,
                                                   "date": "2026-10-17",
                                                   "totalPayment": 89118.15,
                                                   "principalPayment": 85360.52,
                                                   "interestPayment": 3757.63,
                                                   "remainingDebt": 415656.21
                                                 },
                                                 {
                                                   "number": 8,
                                                   "date": "2026-11-17",
                                                   "totalPayment": 89118.15,
                                                   "principalPayment": 86000.73,
                                                   "interestPayment": 3117.42,
                                                   "remainingDebt": 329655.49
                                                 },
                                                 {
                                                   "number": 9,
                                                   "date": "2026-12-17",
                                                   "totalPayment": 89118.15,
                                                   "principalPayment": 86645.73,
                                                   "interestPayment": 2472.42,
                                                   "remainingDebt": 243009.76
                                                 },
                                                 {
                                                   "number": 10,
                                                   "date": "2027-01-17",
                                                   "totalPayment": 89118.15,
                                                   "principalPayment": 87295.58,
                                                   "interestPayment": 1822.57,
                                                   "remainingDebt": 155714.18
                                                 },
                                                 {
                                                   "number": 11,
                                                   "date": "2027-02-17",
                                                   "totalPayment": 89118.15,
                                                   "principalPayment": 87950.29,
                                                   "interestPayment": 1167.86,
                                                   "remainingDebt": 67763.9
                                                 },
                                                 {
                                                   "number": 12,
                                                   "date": "2027-03-17",
                                                   "totalPayment": 68272.13,
                                                   "principalPayment": 67763.9,
                                                   "interestPayment": 508.23,
                                                   "remainingDebt": 0
                                                 }
                                               ]
                                             }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации входных данных (некорректная дата рождения, паспортные данные)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class),
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
            @ApiResponse(responseCode = "422",
                    description = "Отказ в кредите по результатам скоринга (не трудоустроен, низкий доход, плохая кредитная история)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class),
                            examples = @ExampleObject(
                                    name = "scoringErrorExample",
                                    summary = "Пример ошибки скоринга",
                                    value = """
                                            {
                                                "code": 422,
                                                 "type": "Unprocessable Entity",
                                                 "timestamp": "2026-03-16T23:53:01.2758672",
                                                 "message": "Поле 'birthDate': Отказ в займе - Неверная дата рождения, Клиент должен быть совершеннолетним"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера при обработке запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class),
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
    @PostMapping("/calc")
    public CreditDto calculateCredit(@Valid @RequestBody ScoringDataDto request){
        return creditCalculationService.calculateCredit(request);
    }
}
