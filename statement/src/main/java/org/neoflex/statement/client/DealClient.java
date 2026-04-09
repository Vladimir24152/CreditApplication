package org.neoflex.statement.client;

import org.neoflex.statement.dto.LoanOfferDto;
import org.neoflex.statement.dto.LoanStatementRequestDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface DealClient {

    @PostExchange("/api/v1/deal/statement")
    List<LoanOfferDto> calculateOfPossibleLoanTerms(@RequestBody LoanStatementRequestDto request);

    @PostExchange("/api/v1/deal/offer/select")
    void selectOneOfTheLoanOffers(@RequestBody LoanOfferDto request);
}
