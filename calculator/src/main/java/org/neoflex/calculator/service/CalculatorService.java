package org.neoflex.calculator.service;

import lombok.RequiredArgsConstructor;
import org.neoflex.calculator.config.LoanCalculatorProperties;
import org.neoflex.calculator.dto.CreditDto;
import org.neoflex.calculator.dto.LoanOfferDto;
import org.neoflex.calculator.dto.LoanStatementRequestDto;
import org.neoflex.calculator.dto.ScoringDataDto;
import org.neoflex.calculator.exception.NotValidBirthDateException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalculatorService {

    private final LoanCalculatorProperties calculatorProperties;

    public List<LoanOfferDto> calculateLoanOffers(LoanStatementRequestDto request){

        isValidBirthDate(request);

        List<LoanOfferDto> offers = new ArrayList<>();

        offers.add(createOffer(request, true,true));
        offers.add(createOffer(request, false,true));
        offers.add(createOffer(request, true,false));
        offers.add(createOffer(request, false,false));

        return offers.stream()
                .sorted(Comparator.comparing(LoanOfferDto::getTotalAmount))
                .collect(Collectors.toList());
    }

    private LoanOfferDto createOffer(LoanStatementRequestDto request, boolean isInsuranceEnabled, boolean isSalaryClient) {

        BigDecimal totalAmount = calculateTotalAmount(request,isInsuranceEnabled);

        BigDecimal rate = calculateRate(isInsuranceEnabled, isSalaryClient);

        BigDecimal monthlyPayment = calculateMonthlyPayment(totalAmount, request.getTerm(), rate);

        return LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(request.getAmount())
                .totalAmount(totalAmount)
                .term(request.getTerm())
                .monthlyPayment(monthlyPayment)
                .rate(rate)
                .isInsuranceEnabled(isInsuranceEnabled)
                .isSalaryClient(isSalaryClient)
                .build();
    }



    private BigDecimal calculateTotalAmount(LoanStatementRequestDto request, boolean isInsuranceEnabled) {
        if (!isInsuranceEnabled) return request.getAmount();
        BigDecimal insuranceCost = request.getAmount().multiply(calculatorProperties.getInsuranceCostPercent().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        return request.getAmount().add(insuranceCost).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRate(boolean isInsuranceEnabled, boolean isSalaryClient) {
        BigDecimal rate = calculatorProperties.getBaseRate();

        if (isInsuranceEnabled) {
            rate = rate.subtract(calculatorProperties.getInsuranceRateDiscount());
        }

        if (isSalaryClient) {
            rate = rate.subtract(calculatorProperties.getSalaryClientDiscount());
        }

        return rate.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal totalAmount, Integer term, BigDecimal rate) {

        BigDecimal monthlyInterestRate = rate.divide(new BigDecimal(1200),2,RoundingMode.UP);

        BigDecimal annuityRatio = monthlyInterestRate.multiply(monthlyInterestRate.add(new BigDecimal(1)).pow(term))
                .divide(monthlyInterestRate.add(new BigDecimal(1)).pow(term - 1),2,RoundingMode.UP);

        return totalAmount.multiply(annuityRatio);
    }

    public CreditDto calculateCredit(ScoringDataDto request) {

        return null;
    }

    private void isValidBirthDate(LoanStatementRequestDto request){
        LocalDate birthDate = LocalDate.parse(request.getBirthDate());

        if(LocalDate.now().minusYears(18).isBefore(birthDate)){
            throw new NotValidBirthDateException("Неверная дата рождения, дата рождения должна быть ранее 18 лет от текущей даты");
        }
    }
}
