package org.neoflex.calculator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.calculator.constants.LoanCalculatorConstants;
import org.neoflex.calculator.dto.ScoringDataDto;
import org.neoflex.calculator.dto.response.CreditDto;
import org.neoflex.calculator.dto.response.PaymentScheduleElementDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditCalculationService {

    private final CreditCalculator creditCalculator;

    public CreditDto calculateCredit(ScoringDataDto request) {

        if (request == null) {
            throw new NullPointerException("Отсутствует тело запроса");
        }

        log.info("Получен запрос на расчет кредитных условий: сумма = {}, срок = {} мес, имя = {}, фамилия = {}",
                request.getAmount(), request.getTerm(), request.getFirstName(), request.getLastName());

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
                .amount(request.getAmount().setScale(CreditCalculator.RESULT_SCALE, RoundingMode.HALF_UP))
                .term(request.getTerm())
                .monthlyPayment(monthlyPayment.setScale(CreditCalculator.RESULT_SCALE, RoundingMode.HALF_UP))
                .rate(finalRate.setScale(CreditCalculator.RESULT_SCALE, RoundingMode.HALF_UP))
                .psk(psk.setScale(CreditCalculator.RESULT_SCALE, RoundingMode.HALF_UP))
                .isInsuranceEnabled(request.getIsInsuranceEnabled())
                .isSalaryClient(request.getIsSalaryClient())
                .paymentSchedule(paymentSchedule)
                .build();

        log.info("Составлено кредитное предложение: сумма = {}, ПСК = {}, срок = {} мес, процентная ставка = {}",
                creditDto.getAmount(),creditDto.getPsk(), creditDto.getTerm(), creditDto.getRate());

        return creditDto;
    }

    private BigDecimal calculateTotalRate(ScoringDataDto request) {

        BigDecimal finalRate = LoanCalculatorConstants.BASE_RATE;

        finalRate = switch (request.getEmployment().getEmploymentStatus()) {
            case SELF_EMPLOYED -> finalRate.add(LoanCalculatorConstants.SELF_EMPLOY_RATE_ADD);
            case BUSINESS_OWNER -> finalRate.add(LoanCalculatorConstants.BUSINESS_OWNER_RATE_ADD);
            default -> finalRate;
        };

        finalRate = switch (request.getEmployment().getPosition()) {
            case MID_MANAGER -> finalRate.subtract(LoanCalculatorConstants.MID_MANAGER_RATE_DISCOUNT);
            case TOP_MANAGER -> finalRate.subtract(LoanCalculatorConstants.TOP_MANAGER_RATE_DISCOUNT);
            default -> finalRate;
        };

        finalRate = switch (request.getMaritalStatus()) {
            case MARRIED -> finalRate.subtract(LoanCalculatorConstants.MARRIED_RATE_DISCOUNT);
            case DIVORCED -> finalRate.add(LoanCalculatorConstants.DIVORCED_RATE_ADD);
            default -> finalRate;
        };

        int age = Period.between(request.getBirthDate(), LocalDate.now()).getYears();

        switch (request.getGender()){
            case MALE:
                if (age >= 30 && age <= 55) {
                    finalRate = finalRate.subtract(LoanCalculatorConstants.MALE_RATE_DISCOUNT);
                }
                break;
            case FEMALE:
                if (age >= 32 && age <= 60) {
                    finalRate = finalRate.subtract(LoanCalculatorConstants.FEMALE_RATE_DISCOUNT);
                }
                break;
            case NOT_BINARY:
                finalRate = finalRate.add(LoanCalculatorConstants.NOT_BINARY_RATE_ADD);
                break;
        }

        if (request.getIsInsuranceEnabled() == null) {
            throw new NullPointerException("Отсутствует информация о страховке");
        }

        if (request.getIsInsuranceEnabled()){
            finalRate = finalRate.subtract(LoanCalculatorConstants.INSURANCE_RATE_DISCOUNT);
        }

        log.debug("Процентная ставка по кредиту расчитана в сумме = {}",finalRate);
        return finalRate;
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal amount, Integer term, BigDecimal finalRate, Boolean isInsuranceEnabled) {

        System.out.println(amount + ", " + term + ", " + finalRate);
        BigDecimal monthlyPayment = creditCalculator.calculateMonthlyPayment(amount,term,finalRate);
        System.out.println(monthlyPayment);

        if (isInsuranceEnabled == null) {
            throw new NullPointerException("Отсутствует информация о страховке");
        }

        if (isInsuranceEnabled){
            monthlyPayment = monthlyPayment.add(amount.multiply(LoanCalculatorConstants.INSURANCE_COST_PERCENT
                            .divide(CreditCalculator.PERCENT_DIVISOR,CreditCalculator.CALC_SCALE, RoundingMode.HALF_UP))
                    .divide(new BigDecimal(term),CreditCalculator.CALC_SCALE, RoundingMode.HALF_UP));
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

        BigDecimal monthlyRate = creditCalculator.calculateMonthlyRate(rate);

        BigDecimal remainingDebt = amount;

        LocalDate paymentDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= term; i++) {
            BigDecimal interestPayment = remainingDebt
                    .multiply(monthlyRate)
                    .setScale(CreditCalculator.RESULT_SCALE, RoundingMode.HALF_UP);

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
                    .totalPayment(totalPayment.setScale(CreditCalculator.RESULT_SCALE, RoundingMode.HALF_UP))
                    .principalPayment(principalPayment.setScale(CreditCalculator.RESULT_SCALE, RoundingMode.HALF_UP))
                    .interestPayment(interestPayment.setScale(CreditCalculator.RESULT_SCALE, RoundingMode.HALF_UP))
                    .remainingDebt(remainingDebt.setScale(CreditCalculator.RESULT_SCALE, RoundingMode.HALF_UP))
                    .build();

            schedule.add(element);

            paymentDate = paymentDate.plusMonths(1);
        }

        return schedule;
    }

    private static BigDecimal calculatePsk(BigDecimal monthlyPayment, Integer term) {
        return monthlyPayment.multiply(new BigDecimal(term));
    }
}
