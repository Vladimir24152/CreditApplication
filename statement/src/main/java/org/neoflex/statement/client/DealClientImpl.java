package org.neoflex.statement.client;

import lombok.RequiredArgsConstructor;
import org.neoflex.statement.dto.LoanOfferDto;
import org.neoflex.statement.dto.LoanStatementRequestDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DealClientImpl {

    private final DealClient dealClient;

    public List<LoanOfferDto> calculateOfPossibleLoanTerms(LoanStatementRequestDto request) {
        return dealClient.calculateOfPossibleLoanTerms(request);
    }

    public void selectOffer(LoanOfferDto request) {
        dealClient.selectOffer(request);
    }
}
