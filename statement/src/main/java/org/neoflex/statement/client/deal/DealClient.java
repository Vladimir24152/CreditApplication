package org.neoflex.statement.client.deal;

import org.neoflex.statement.dto.LoanOfferDto;
import org.neoflex.statement.dto.LoanStatementRequestDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange("${client.deal.url}")
public interface DealClient {

    @PostExchange("${client.deal.api.calculate.path}")
    List<LoanOfferDto> calculateOfPossibleLoanTerms(@RequestBody LoanStatementRequestDto request);

    @PostExchange("${client.deal.api.select.path}")
    void selectOffer(@RequestBody LoanOfferDto request);
}
