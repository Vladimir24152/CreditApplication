package org.neoflex.statement.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.neoflex.statement.client.deal.DealClientService;
import org.neoflex.statement.dto.LoanOfferDto;
import org.neoflex.statement.dto.LoanStatementRequestDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatementService {

    private final DealClientService dealClientService;

    public List<LoanOfferDto> getOffers(@NonNull LoanStatementRequestDto request) {
        return dealClientService.calculateOfPossibleLoanTerms(request);
    }

    public void selectOffer(@NonNull LoanOfferDto request) {
        dealClientService.selectOffer(request);
    }
}
