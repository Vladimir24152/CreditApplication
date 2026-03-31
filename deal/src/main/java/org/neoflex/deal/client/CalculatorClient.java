package org.neoflex.deal.client;

import org.neoflex.deal.dto.CreditDto;
import org.neoflex.deal.dto.LoanOfferDto;
import org.neoflex.deal.dto.LoanStatementRequestDto;
import org.neoflex.deal.dto.ScoringDataDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface CalculatorClient {

    @PostExchange("/api/v1/calculator/offers")
    List<LoanOfferDto> calculateLoanOffers(@RequestBody LoanStatementRequestDto request);

    @PostExchange("/api/v1/calculator/calc")
    CreditDto calculateCredit(@RequestBody ScoringDataDto request);
}
