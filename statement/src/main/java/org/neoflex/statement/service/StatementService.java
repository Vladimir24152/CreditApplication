package org.neoflex.statement.service;

import lombok.RequiredArgsConstructor;
import org.neoflex.statement.client.DealClientImpl;
import org.neoflex.statement.dto.LoanOfferDto;
import org.neoflex.statement.dto.LoanStatementRequestDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatementService {

    private final DealClientImpl dealClientImpl;

    public List<LoanOfferDto> getOffers(LoanStatementRequestDto request) {

        if (request == null) {
            throw new NullPointerException("Отсутствует тело запроса");
        }

        return dealClientImpl.calculateOfPossibleLoanTerms(request);
    }

    public void selectOffer(LoanOfferDto request) {

        if (request == null) {
            throw new NullPointerException("Отсутствует тело запроса");
        }

        dealClientImpl.selectOffer(request);
    }
}
