package org.neoflex.calculator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.calculator.config.LoanCalculatorProperties;
import org.neoflex.calculator.dto.CreditDto;
import org.neoflex.calculator.dto.LoanStatementRequestDto;
import org.neoflex.calculator.dto.PaymentScheduleElementDto;
import org.neoflex.calculator.dto.ScoringDataDto;
import org.neoflex.calculator.exception.NotValidBirthDateException;
import org.neoflex.calculator.exception.ScrollingFailed;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import static org.neoflex.calculator.enums.EmploymentStatus.UNEMPLOYED;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditCalculationService {

    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("12");
    private static final BigDecimal PERCENT_DIVISOR = new BigDecimal("100");
    private static final BigDecimal MAXIMUM_LOAN_AMOUNT_IN_SALARIES = new BigDecimal("24");
    private static final int MAX_AGE_FOR_LOAD = 65;
    private static final int MIN_AGE_FOR_LOAD = 20;
    private static final int MIN_EXPERIENCE_TOTAL_FOR_LOAD = 18;
    private static final int MIN_EXPERIENCE_CURRENT_FOR_LOAD = 3;
    private static final int CALC_SCALE = 5;
    private static final int RESULT_SCALE = 2;

    private final LoanCalculatorProperties calculatorProperties;

    public CreditDto calculateCredit(ScoringDataDto request) {

        checkingTheLoanApplication(request);

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
                .amount(request.getAmount().setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                .term(request.getTerm())
                .monthlyPayment(monthlyPayment.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                .rate(finalRate.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                .psk(psk.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                .isInsuranceEnabled(request.getIsInsuranceEnabled())
                .isSalaryClient(request.getIsSalaryClient())
                .paymentSchedule(paymentSchedule)
                .build();

        return creditDto;
    }

    private void checkingTheLoanApplication(ScoringDataDto request) {
        if (request.getEmployment().getEmploymentStatus().equals(UNEMPLOYED)){
            throw new ScrollingFailed("Отказ в предоставлении займа не трудоустроенным");
        }

        if (request.getEmployment().getSalary().multiply(MAXIMUM_LOAN_AMOUNT_IN_SALARIES).compareTo(request.getAmount()) < 0){
            throw new ScrollingFailed("Отказ в предоставлении займа превышающего среднемесячный доход более чем в "
                    + MAXIMUM_LOAN_AMOUNT_IN_SALARIES + "раза(раз)");
        }

        int age = Period.between(request.getBirthDate(), LocalDate.now()).getYears();

        if (age < MIN_AGE_FOR_LOAD){
            throw new ScrollingFailed(String.format("Отказ в предоставлении займа клиентам младше %d лет",MIN_AGE_FOR_LOAD));
        }

        if (age > MAX_AGE_FOR_LOAD){
            throw new ScrollingFailed(String.format("Отказ в предоставлении займа клиентам старше %d лет",MAX_AGE_FOR_LOAD));
        }

        if (request.getEmployment().getWorkExperienceTotal() < MIN_EXPERIENCE_TOTAL_FOR_LOAD){
            throw new ScrollingFailed(String.format("Отказ в предоставлении займа клиентам с общим стажем работы менее %d месяцев",MIN_EXPERIENCE_TOTAL_FOR_LOAD));
        }

        if (request.getEmployment().getWorkExperienceCurrent() < MIN_EXPERIENCE_CURRENT_FOR_LOAD){
            throw new ScrollingFailed(String.format("Отказ в предоставлении займа клиентам с текущем стажем работы менее %d месяцев",MIN_EXPERIENCE_CURRENT_FOR_LOAD));
        }
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

        return finalRate;
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal amount, Integer term, BigDecimal finalRate, Boolean getIsInsuranceEnabled) {
        BigDecimal monthlyRate = finalRate
                .divide(PERCENT_DIVISOR, CALC_SCALE, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR, CALC_SCALE, RoundingMode.HALF_UP);

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);

        BigDecimal annuityRatio = monthlyRate.multiply(onePlusRate.pow(term))
                .divide(onePlusRate.pow(term).subtract(BigDecimal.ONE), CALC_SCALE, RoundingMode.HALF_UP);

        BigDecimal monthlyPayment =amount.multiply(annuityRatio).setScale(RESULT_SCALE, RoundingMode.HALF_UP);

        if (getIsInsuranceEnabled){
            monthlyPayment = monthlyPayment.add(amount.multiply(calculatorProperties.getInsuranceCostPercent().divide(PERCENT_DIVISOR,CALC_SCALE, RoundingMode.HALF_UP))
                    .divide(new BigDecimal(term),CALC_SCALE, RoundingMode.HALF_UP));
        }

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

        BigDecimal monthlyRate = rate
                .divide(PERCENT_DIVISOR, CALC_SCALE, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR, CALC_SCALE, RoundingMode.HALF_UP);

        BigDecimal remainingDebt = amount;

        LocalDate paymentDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= term; i++) {
            BigDecimal interestPayment = remainingDebt
                    .multiply(monthlyRate)
                    .setScale(RESULT_SCALE, RoundingMode.HALF_UP);

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
                    .totalPayment(totalPayment.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                    .principalPayment(principalPayment.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                    .interestPayment(interestPayment.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                    .remainingDebt(remainingDebt.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                    .build();

            schedule.add(element);

            paymentDate = paymentDate.plusMonths(1);
        }

        return schedule;
    }
}
