package org.neoflex.statement.client.deal;

import org.neoflex.statement.dto.LoanOfferDto;
import org.neoflex.statement.dto.LoanStatementRequestDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange("${deal.endpoints.base}")
public interface DealClient {

    @PostExchange("${deal.endpoints.statement}")
    List<LoanOfferDto> calculateOfPossibleLoanTerms(@RequestBody LoanStatementRequestDto request);

    @PostExchange("${deal.endpoints.offer-select}")
    void selectOffer(@RequestBody LoanOfferDto request);
}
