package org.neoflex.calculator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCalculator {

    public static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("12");
    public static final BigDecimal PERCENT_DIVISOR = new BigDecimal("100");
    public static final int CALC_SCALE = 10;
    public static final int RESULT_SCALE = 2;

    public BigDecimal calculateMonthlyRate(BigDecimal rate){
        return rate
                .divide(PERCENT_DIVISOR, CALC_SCALE, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR, CALC_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateMonthlyPayment(BigDecimal amount, Integer term, BigDecimal finalRate) {
        BigDecimal monthlyRate = calculateMonthlyRate(finalRate);

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);

        BigDecimal annuityRatio = monthlyRate.multiply(onePlusRate.pow(term))
                .divide(onePlusRate.pow(term).subtract(BigDecimal.ONE), CALC_SCALE, RoundingMode.HALF_UP);

        BigDecimal monthlyPayment =amount.multiply(annuityRatio).setScale(RESULT_SCALE, RoundingMode.HALF_UP);

        log.debug("Аннуитетный платеж по кредиту = {}",monthlyPayment);
        return monthlyPayment;
    }
}
