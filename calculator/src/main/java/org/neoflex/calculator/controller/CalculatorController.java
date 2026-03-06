package org.neoflex.calculator.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.neoflex.calculator.dto.CreditDto;
import org.neoflex.calculator.dto.LoanOfferDto;
import org.neoflex.calculator.dto.LoanStatementRequestDto;
import org.neoflex.calculator.dto.ScoringDataDto;
import org.neoflex.calculator.service.CalculatorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.neoflex.calculator.constant.ApiConstant.CALS_URL;
import static org.neoflex.calculator.constant.ApiConstant.OFFERS_URL;
import static org.neoflex.calculator.constant.ApiConstant.CALCULATOR_URL;

@RestController
@RequiredArgsConstructor
@RequestMapping(CALCULATOR_URL)
public class CalculatorController {

    private final CalculatorService calculatorService;

    /**
     * Расчёт возможных условий кредита на основе предварительных данных
     * @param request DTO с базовыми данными заявки
     * @return список кредитных предложений (4 варианта: с/без страховки, с/без зарплатного клиента)
     */
    @Operation(description = "Расчёт возможных условий кредита")
    @PostMapping(OFFERS_URL)
    private List<LoanOfferDto> calculateLoanOffers(@Valid @RequestBody LoanStatementRequestDto request){
        return calculatorService.calculateLoanOffers(request);
    }

    /**
     * Полный скоринг и расчет параметров кредита
     * @param request DTO с полными данными для скоринга
     * @return рассчитанный кредит с графиком платежей
     */
    @Operation(description = "валидация присланных данных, скоринг данных, полный расчет параметров кредита")
    @PostMapping(CALS_URL)
    private CreditDto calculateCredit(@Valid @RequestBody ScoringDataDto request){// <- Сделать валидацию в дто
        return calculatorService.calculateCredit(request);
    }
}
