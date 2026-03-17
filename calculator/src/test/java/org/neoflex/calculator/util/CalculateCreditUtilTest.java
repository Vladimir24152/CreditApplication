package org.neoflex.calculator.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты утилиты CalculateCreditUtil")
class CalculateCreditUtilTest {

    private final BigDecimal rate = new BigDecimal("12");
    private final BigDecimal maximalErrancy = new BigDecimal("0.10");

    @Test
    @DisplayName("Успешный расчет ежемесячной ставки при корректной годовой ставке")
    void whenAnnualRateIsValidThenCalculateMonthlyRateReturnsCorrectValue() {
        BigDecimal expected = new BigDecimal("0.01");

        BigDecimal result = CalculateCreditUtil.calculateMonthlyRate(rate);

        assertEquals(0, expected.compareTo(result),
                "Ежемесячная ставка должна быть 0.01 (12%/12/100)");
    }

    @Test
    @DisplayName("Успешный расчет ПСК при корректных ежемесячном платеже и сроке")
    void whenMonthlyPaymentAndTermAreValidThenCalculatePskReturnsCorrectValue() {
        BigDecimal monthlyPayment = new BigDecimal("10000");
        Integer term = 12;
        BigDecimal expected = new BigDecimal("120000");

        BigDecimal result = CalculateCreditUtil.calculatePsk(monthlyPayment, term);

        assertEquals(0, expected.compareTo(result));
    }


    @Test
    @DisplayName("Успешный расчет ежемесячного аннуитетного платежа")
    void whenAmountTermAndRateAreValidThenCalculateMonthlyPaymentReturnsCorrectValue() {

        BigDecimal result = CalculateCreditUtil.calculateMonthlyPayment(new BigDecimal("1000000"), 12, rate);

        BigDecimal expected = new BigDecimal("88848.89");


        assertTrue(expected.subtract(result).compareTo(maximalErrancy) <= 0,
                String.format("Ожидаемый платеж: %s, Фактический: %s", expected, result));
    }

    @Test
    @DisplayName("Клиент старше 18 лет - проверка совершеннолетия проходит успешно")
    void whenTheClientIsOver18YearsOfAgeThenIsValidBirthDateReturnsFalse() {
        LocalDate birthDate = LocalDate.now().minusYears(30);

        Boolean result = CalculateCreditUtil.isValidBirthDate(birthDate);

        assertFalse(result, "Клиент старше 18 лет должен проходить проверку");
    }

    @Test
    @DisplayName("Клиент младше 18 лет - проверка совершеннолетия не проходит")
    void whenTheClientIsUnder18YearsOfAge_thenIsValidBirthDateReturnsTrue() {
        LocalDate birthDate = LocalDate.now().minusYears(17);

        Boolean result = CalculateCreditUtil.isValidBirthDate(birthDate);

        assertTrue(result, "Клиент младше 18 лет не должен проходить проверку");
    }

    @Test
    @DisplayName("Клиенту ровно 18 лет - проверка совершеннолетия проходит")
    void whenTheClientIsExactly18YearsOfAge_thenIsValidBirthDateReturnsFalse() {
        LocalDate birthDate = LocalDate.now().minusYears(18);

        Boolean result = CalculateCreditUtil.isValidBirthDate(birthDate);

        assertFalse(result, "Клиенту ровно 18 лет должен проходить проверку");
    }
}