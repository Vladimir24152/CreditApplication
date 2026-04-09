package org.neoflex.statement.service;

import lombok.RequiredArgsConstructor;
import org.neoflex.statement.dto.LoanOfferDto;
import org.neoflex.statement.dto.LoanStatementRequestDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatementService {

    private final DealClientService dealClientService;

    public List<LoanOfferDto> preScoringAndGetOffers(LoanStatementRequestDto request) {

        if (request == null) {
            throw new NullPointerException("Отсутствует тело запроса");
        }

        return dealClientService.calculateOfPossibleLoanTerms(request);
    }

    public void selectOffer(LoanOfferDto request) {

        if (request == null) {
            throw new NullPointerException("Отсутствует тело запроса");
        }

        dealClientService.selectOneOfTheLoanOffers(request);
    }
}
