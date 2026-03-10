package org.neoflex.calculator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.calculator.config.LoanCalculatorProperties;
import org.neoflex.calculator.dto.CreditDto;
import org.neoflex.calculator.dto.PaymentScheduleElementDto;
import org.neoflex.calculator.dto.ScoringDataDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import static org.neoflex.calculator.enums.Gender.FEMALE;
import static org.neoflex.calculator.enums.Gender.MALE;
import static org.neoflex.calculator.enums.Gender.NOT_BINARY;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditCalculationService {

    private final LoanCalculatorProperties calculatorProperties;

    public CreditDto calculateCredit(ScoringDataDto request) {

        BigDecimal finalRate = calculateTotalRate(request);

        BigDecimal monthlyPayment = calculateMonthlyPayment(
                request.getAmount(),
                request.getTerm(),
                finalRate,
                request.getIsInsuranceEnabled()
        );

        BigDecimal psk = calculatePsk(monthlyPayment, request.getTerm());

        List<PaymentScheduleElementDto> paymentSchedule = calculatePaymentSchedule(
                request.getAmount(),
                finalRate,
                request.getTerm(),
                monthlyPayment
        );


        CreditDto creditDto = CreditDto.builder()
                .amount(request.getAmount().setScale(2, RoundingMode.HALF_UP))
                .term(request.getTerm())
                .monthlyPayment(monthlyPayment.setScale(2, RoundingMode.HALF_UP))
                .rate(finalRate.setScale(2, RoundingMode.HALF_UP))
                .psk(psk.setScale(2, RoundingMode.HALF_UP))
                .isInsuranceEnabled(request.getIsInsuranceEnabled())
                .isSalaryClient(request.getIsSalaryClient())
                .paymentSchedule(paymentSchedule)
                .build();

        return creditDto;
    }

    private BigDecimal calculateTotalRate(ScoringDataDto request) {

        BigDecimal finalRate = calculatorProperties.getBaseRate();

        switch (request.getEmployment().getEmploymentStatus()){
            case SELF_EMPLOYED:
                finalRate = finalRate = finalRate.add(calculatorProperties.getSelfEmploeRateAdd());
                break;
            case BUSINESS_OWNER:
                finalRate = finalRate.add(calculatorProperties.getBusinesOwnerRateAdd());
                break;
        }

        switch (request.getEmployment().getPosition()){
            case MID_MANAGER:
                finalRate = finalRate.subtract(calculatorProperties.getMidManagerRateDiscount());
                break;
            case TOP_MANAGER:
                finalRate = finalRate.subtract(calculatorProperties.getTopManagerRateDiscount());
                break;
        }

        switch (request.getMaritalStatus()){
            case MARRIED:
                finalRate = finalRate.subtract(calculatorProperties.getMarriedRateDiscount());
                break;
            case DIVORCED:
                finalRate = finalRate.add(calculatorProperties.getDivorcedRateAdd());
                break;
        }

        int age = Period.between(request.getBirthDate(), LocalDate.now()).getYears();

        switch (request.getGender()){
            case MALE:
                if (age >= 30 && age <= 55) {
                    finalRate = finalRate.subtract(calculatorProperties.getMaleRateDiscount());
                }
                break;
            case FEMALE:
                if (age >= 32 && age <= 60) {
                    finalRate = finalRate.subtract(calculatorProperties.getFemaleRateDiscount());
                }
                break;
            case NOT_BINARY:
                finalRate = finalRate.add(calculatorProperties.getNotBinaryRateAdd());
                break;
        }

        if (request.getIsInsuranceEnabled()){
            finalRate = finalRate.subtract(calculatorProperties.getInsuranceRateDiscount());
        }
        log.info("Rate {}",finalRate);
        return finalRate;
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal amount, Integer term, BigDecimal finalRate, Boolean getIsInsuranceEnabled) {
        BigDecimal monthlyInterestRate = finalRate.divide(new BigDecimal(1200),5, RoundingMode.HALF_UP);

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyInterestRate);

        BigDecimal annuityRatio = monthlyInterestRate.multiply(onePlusRate.pow(term))
                .divide(onePlusRate.pow(term).subtract(BigDecimal.ONE), 10, RoundingMode.HALF_UP);

        BigDecimal monthlyPayment =amount.multiply(annuityRatio).setScale(2, RoundingMode.HALF_UP);

        log.info("monthlyPayment without Insurance {}",monthlyPayment);

        log.info("InsuranceCostPercent {}",calculatorProperties.getInsuranceCostPercent().divide(new BigDecimal(100),5, RoundingMode.HALF_UP));
        if (getIsInsuranceEnabled){
            monthlyPayment = monthlyPayment.add(amount.multiply(calculatorProperties.getInsuranceCostPercent().divide(new BigDecimal(100),5, RoundingMode.HALF_UP))
                    .divide(new BigDecimal(term),5, RoundingMode.HALF_UP));
        }

        log.info("monthlyPayment with Insurance {}",monthlyPayment);
        return monthlyPayment;
    }

    private BigDecimal calculatePsk(BigDecimal monthlyPayment, Integer term) {
        return monthlyPayment.multiply(new BigDecimal(term));
    }

    private List<PaymentScheduleElementDto> calculatePaymentSchedule(
            BigDecimal amount,
            BigDecimal rate,
            Integer term,
            BigDecimal monthlyPayment) {

        List<PaymentScheduleElementDto> schedule = new ArrayList<>();

        BigDecimal monthlyRate = rate.divide(new BigDecimal("1200"), 5, RoundingMode.HALF_UP);

        BigDecimal remainingDebt = amount;

        LocalDate paymentDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= term; i++) {
            BigDecimal interestPayment = remainingDebt
                    .multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principalPayment;
            BigDecimal totalPayment;

            if (i == term) {
                principalPayment = remainingDebt;
                totalPayment = principalPayment.add(interestPayment);
            } else {
                principalPayment = monthlyPayment.subtract(interestPayment);
                totalPayment = monthlyPayment;
            }

            remainingDebt = remainingDebt.subtract(principalPayment);

            PaymentScheduleElementDto element = PaymentScheduleElementDto.builder()
                    .number(i)
                    .date(paymentDate)
                    .totalPayment(totalPayment.setScale(2, RoundingMode.HALF_UP))
                    .principalPayment(principalPayment.setScale(2, RoundingMode.HALF_UP))
                    .interestPayment(interestPayment.setScale(2, RoundingMode.HALF_UP))
                    .remainingDebt(remainingDebt.setScale(2, RoundingMode.HALF_UP))
                    .build();

            schedule.add(element);

            paymentDate = paymentDate.plusMonths(1);
        }

        return schedule;
    }
}
