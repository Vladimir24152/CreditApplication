package org.neoflex.calculator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.calculator.config.LoanCalculatorProperties;
import org.neoflex.calculator.dto.CreditDto;
import org.neoflex.calculator.dto.PaymentScheduleElementDto;
import org.neoflex.calculator.dto.ScoringDataDto;
import org.neoflex.calculator.exception.NotValidBirthDateException;
import org.neoflex.calculator.exception.ScoringFailedException;
import org.neoflex.calculator.util.CalculateCreditUtil;
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

        log.info("Получен запрос на расчет кредитных условий: сумма = {}, срок = {} мес, имя = {}, фамилия = {}",
                request.getAmount(), request.getTerm(), request.getFirstName(), request.getLastName());

        checkingTheLoanApplication(request);

        BigDecimal finalRate = calculateTotalRate(request);

        BigDecimal monthlyPayment = calculateMonthlyPayment(
                request.getAmount(),
                request.getTerm(),
                finalRate,
                request.getIsInsuranceEnabled()
        );

        BigDecimal psk = CalculateCreditUtil.calculatePsk(monthlyPayment, request.getTerm());

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

        log.info("Составлено кредитное предложение: сумма = {}, ПСК = {}, срок = {} мес, процентная ставка = {}",
                creditDto.getAmount(),creditDto.getPsk(), creditDto.getTerm(), creditDto.getRate());

        return creditDto;
    }

    private void checkingTheLoanApplication(ScoringDataDto request) {

        if (CalculateCreditUtil.isValidBirthDate(request.getBirthDate())){
            throw new NotValidBirthDateException("Неверная дата рождения, Клиент должен быть совершеннолетним");
        }

        if (request.getEmployment().getEmploymentStatus().equals(UNEMPLOYED)){
            throw new ScoringFailedException("Отказ в предоставлении займа не трудоустроенным");
        }

        if (request.getEmployment().getSalary().multiply(MAXIMUM_LOAN_AMOUNT_IN_SALARIES).compareTo(request.getAmount()) < 0){
            throw new ScoringFailedException("Отказ в предоставлении займа превышающего среднемесячный доход более чем в "
                    + MAXIMUM_LOAN_AMOUNT_IN_SALARIES + "раза(раз)");
        }

        int age = Period.between(request.getBirthDate(), LocalDate.now()).getYears();

        if (age < MIN_AGE_FOR_LOAD){
            throw new ScoringFailedException(String.format("Отказ в предоставлении займа клиентам младше %d лет",MIN_AGE_FOR_LOAD));
        }

        if (age > MAX_AGE_FOR_LOAD){
            throw new ScoringFailedException(String.format("Отказ в предоставлении займа клиентам старше %d лет",MAX_AGE_FOR_LOAD));
        }

        if (request.getEmployment().getWorkExperienceTotal() < MIN_EXPERIENCE_TOTAL_FOR_LOAD){
            throw new ScoringFailedException(String.format("Отказ в предоставлении займа клиентам с общим стажем работы менее %d месяцев",MIN_EXPERIENCE_TOTAL_FOR_LOAD));
        }

        if (request.getEmployment().getWorkExperienceCurrent() < MIN_EXPERIENCE_CURRENT_FOR_LOAD){
            throw new ScoringFailedException(String.format("Отказ в предоставлении займа клиентам с текущем стажем работы менее %d месяцев",MIN_EXPERIENCE_CURRENT_FOR_LOAD));
        }
    }

    private BigDecimal calculateTotalRate(ScoringDataDto request) {

        BigDecimal finalRate = calculatorProperties.getBaseRate();

        finalRate = switch (request.getEmployment().getEmploymentStatus()) {
            case SELF_EMPLOYED -> finalRate.add(calculatorProperties.getSelfEmployRateAdd());
            case BUSINESS_OWNER -> finalRate.add(calculatorProperties.getBusinessOwnerRateAdd());
            default -> finalRate;
        };

        finalRate = switch (request.getEmployment().getPosition()) {
            case MID_MANAGER -> finalRate.subtract(calculatorProperties.getMidManagerRateDiscount());
            case TOP_MANAGER -> finalRate.subtract(calculatorProperties.getTopManagerRateDiscount());
            default -> finalRate;
        };

        finalRate = switch (request.getMaritalStatus()) {
            case MARRIED -> finalRate.subtract(calculatorProperties.getMarriedRateDiscount());
            case DIVORCED -> finalRate.add(calculatorProperties.getDivorcedRateAdd());
            default -> finalRate;
        };

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

        log.debug("Процентная ставка по кредиту расчитана в сумме = {}",finalRate);
        return finalRate;
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal amount, Integer term, BigDecimal finalRate, Boolean isInsuranceEnabled) {

        BigDecimal monthlyPayment =CalculateCreditUtil.calculateMonthlyPayment(amount,term,finalRate);

        if (isInsuranceEnabled){
            monthlyPayment = monthlyPayment.add(amount.multiply(calculatorProperties.getInsuranceCostPercent()
                            .divide(PERCENT_DIVISOR,CALC_SCALE, RoundingMode.HALF_UP))
                    .divide(new BigDecimal(term),CALC_SCALE, RoundingMode.HALF_UP));
        }

        log.debug("Аннуитетный платеж по кредиту = {}",monthlyPayment);
        return monthlyPayment;
    }

    private List<PaymentScheduleElementDto> calculatePaymentSchedule(
            BigDecimal amount,
            BigDecimal rate,
            Integer term,
            BigDecimal monthlyPayment) {

        List<PaymentScheduleElementDto> schedule = new ArrayList<>();

        BigDecimal monthlyRate = CalculateCreditUtil.calculateMonthlyRate(rate);

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
