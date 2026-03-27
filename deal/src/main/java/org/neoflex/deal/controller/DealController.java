package org.neoflex.deal.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.neoflex.deal.dto.FinishRegistrationRequestDto;
import org.neoflex.deal.dto.LoanOfferDto;
import org.neoflex.deal.dto.LoanStatementRequestDto;
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
    @PostMapping("/statement")
    public List<LoanOfferDto> calculationOfPossibleLoanTerms(@Valid @RequestBody LoanStatementRequestDto request){
        return dealService.calculationOfPossibleLoanTerms(request);
    }

    @Operation(summary = "Выбор одного из кредитных предложений",
            description = "Выбирает конкретное кредитное предложение для дальнейшего оформления")
    @PostMapping("/offer/select")
    public void choosingOneOfTheLoanOffers(@Valid @RequestBody LoanOfferDto request){
        dealService.choosingOneOfTheLoanOffers(request);
    }

    @Operation(summary = "Завершение регистрации и полный расчет кредита",
            description = "Завершает регистрацию клиента и выполняет полный расчет кредита")
    @PostMapping("/calculate/{statementId}")
    public void completionOfRegistrationAndFullCreditCalculation(
            @Valid @RequestBody FinishRegistrationRequestDto request,
            @RequestParam String statementId
    ){

    }
}
