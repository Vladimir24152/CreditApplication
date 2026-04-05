package org.neoflex.calculator.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тесты CreditCalculator")
class CreditCalculatorTest {

    private final BigDecimal rate = new BigDecimal("12");
    private final BigDecimal maximalErrancy = new BigDecimal("0.10");
    private final CreditCalculator creditCalculator = new CreditCalculator();

    @Test
    @DisplayName("Успешный расчет ежемесячной ставки при корректной годовой ставке")
    void whenAnnualRateIsValidThenCalculateMonthlyRateReturnsCorrectValue() {
        BigDecimal expected = new BigDecimal("0.01");

        BigDecimal result = creditCalculator.calculateMonthlyRate(rate);

        assertEquals(0, expected.compareTo(result),
                "Ежемесячная ставка должна быть 0.01 (12%/12/100)");
    }

    @Test
    @DisplayName("Успешный расчет ежемесячного аннуитетного платежа")
    void whenAmountTermAndRateAreValidThenCalculateMonthlyPaymentReturnsCorrectValue() {

        BigDecimal result = creditCalculator.calculateMonthlyPayment(new BigDecimal("1000000"), 12, rate);

        BigDecimal expected = new BigDecimal("88848.89");


        assertTrue(expected.subtract(result).compareTo(maximalErrancy) <= 0,
                String.format("Ожидаемый платеж: %s, Фактический: %s", expected, result));
    }
}