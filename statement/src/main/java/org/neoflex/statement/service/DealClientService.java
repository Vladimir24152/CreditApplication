package org.neoflex.statement.service;

import lombok.RequiredArgsConstructor;
import org.neoflex.statement.client.DealClient;
import org.neoflex.statement.dto.LoanOfferDto;
import org.neoflex.statement.dto.LoanStatementRequestDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DealClientService {

    private final DealClient dealClient;

    List<LoanOfferDto> calculateOfPossibleLoanTerms(LoanStatementRequestDto request) {
        return dealClient.calculateOfPossibleLoanTerms(request);
    }

    void selectOneOfTheLoanOffers(LoanOfferDto request) {
        dealClient.selectOneOfTheLoanOffers(request);
    }
}
