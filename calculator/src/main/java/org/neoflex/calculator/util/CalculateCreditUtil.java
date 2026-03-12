package org.neoflex.calculator.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Slf4j
@UtilityClass
public class CalculateCreditUtil {

    public static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("12");
    public static final BigDecimal PERCENT_DIVISOR = new BigDecimal("100");
    public static final int CALC_SCALE = 10;
    public static final int RESULT_SCALE = 2;

    public static BigDecimal calculateMonthlyRate(BigDecimal rate){
        return rate
                .divide(PERCENT_DIVISOR, CALC_SCALE, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR, CALC_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculatePsk(BigDecimal monthlyPayment, Integer term) {
        return monthlyPayment.multiply(new BigDecimal(term));
    }


    public static BigDecimal calculateMonthlyPayment(BigDecimal amount, Integer term, BigDecimal finalRate) {
        BigDecimal monthlyRate = calculateMonthlyRate(finalRate);

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);

        BigDecimal annuityRatio = monthlyRate.multiply(onePlusRate.pow(term))
                .divide(onePlusRate.pow(term).subtract(BigDecimal.ONE), CALC_SCALE, RoundingMode.HALF_UP);

        BigDecimal monthlyPayment =amount.multiply(annuityRatio).setScale(RESULT_SCALE, RoundingMode.HALF_UP);

        log.debug("Аннуитетный платеж по кредиту = {}",monthlyPayment);
        return monthlyPayment;
    }

    public Boolean isValidBirthDate(LocalDate birthDate){
        return LocalDate.now().minusYears(18).isBefore(birthDate);
    }
}
