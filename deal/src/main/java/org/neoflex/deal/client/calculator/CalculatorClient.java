package org.neoflex.deal.client.calculator;

import org.neoflex.deal.dto.CreditDto;
import org.neoflex.deal.dto.LoanOfferDto;
import org.neoflex.deal.dto.LoanStatementRequestDto;
import org.neoflex.deal.dto.ScoringDataDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange("${calculator.endpoints.base}")
public interface CalculatorClient {

    @PostExchange("${calculator.endpoints.offers}")
    List<LoanOfferDto> calculateLoanOffers(@RequestBody LoanStatementRequestDto request);

    @PostExchange("${calculator.endpoints.calc}")
    CreditDto calculateCredit(@RequestBody ScoringDataDto request);
}
