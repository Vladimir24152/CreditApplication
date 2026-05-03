package org.neoflex.deal.client.calculator;

import lombok.RequiredArgsConstructor;
import org.neoflex.deal.dto.CreditDto;
import org.neoflex.deal.dto.LoanOfferDto;
import org.neoflex.deal.dto.LoanStatementRequestDto;
import org.neoflex.deal.dto.ScoringDataDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculatorClientService {

    private final CalculatorClient calculatorClient;


    public List<LoanOfferDto> calculateLoanOffers(LoanStatementRequestDto request){
        return calculatorClient.calculateLoanOffers(request);
    }


    public CreditDto calculateCredit(ScoringDataDto request){
        return calculatorClient.calculateCredit(request);
    }
}
