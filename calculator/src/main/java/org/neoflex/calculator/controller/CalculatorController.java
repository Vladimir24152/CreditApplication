package org.neoflex.calculator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.neoflex.calculator.dto.CreditDto;
import org.neoflex.calculator.dto.LoanOfferDto;
import org.neoflex.calculator.dto.LoanStatementRequestDto;
import org.neoflex.calculator.dto.ScoringDataDto;
import org.neoflex.calculator.service.CreditCalculationService;
import org.neoflex.calculator.service.LoanOfferService;
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
            @ApiResponse(responseCode = "200", description = "Кредитные предложения успешно сформированы"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных (неверный формат email, паспорта и т.д.)"),
            @ApiResponse(responseCode = "422", description = "Ошибка скоринга - клиент не проходит по условиям (возраст, стаж, доход)"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/offers")
    private List<LoanOfferDto> calculateLoanOffers(@Valid @RequestBody LoanStatementRequestDto request){
        return loanOfferService.calculateLoanOffers(request);
    }

    @Operation(description = "Валидация присланных данных, скоринг данных, полный расчет параметров кредита")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Кредит успешно рассчитан, возвращен график платежей"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных (некорректная дата рождения, паспортные данные)"),
            @ApiResponse(responseCode = "422", description = "Отказ в кредите по результатам скоринга (не трудоустроен, низкий доход, плохая кредитная история)"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера при обработке запроса")
    })
    @PostMapping("/calc")
    private CreditDto calculateCredit(@Valid @RequestBody ScoringDataDto request){
        return creditCalculationService.calculateCredit(request);
    }
}
