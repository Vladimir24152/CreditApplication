package org.neoflex.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.neoflex.calculator.config.LoanCalculatorProperties;
import org.neoflex.calculator.dto.response.LoanOfferDto;
import org.neoflex.calculator.dto.LoanStatementRequestDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тестирование сервиса формирования кредитных предложений")
class LoanOfferServiceTest {

    private LoanCalculatorProperties properties;

    private LoanOfferService offerService;

    private static final int RESULT_SCALE = 2;
    private static final BigDecimal INSURANCE_RATE_DISCOUNT = new BigDecimal("3.00");
    private static final BigDecimal SALARY_CLIENT_DISCOUNT = new BigDecimal("1.00");

    private LoanStatementRequestDto request;

    @BeforeEach
    void setUp(){
        request = LoanStatementRequestDto.builder()
                .amount(new BigDecimal(1_000_000))
                .term(12)
                .firstName("Ivan")
                .lastName("Ivanov")
                .birthDate(LocalDate.now().minusYears(25))
                .build();

        properties = new LoanCalculatorProperties();

        properties.setBaseRate(new BigDecimal("15.0"));
        properties.setInsuranceRateDiscount(new BigDecimal("3.0"));
        properties.setSalaryClientDiscount(new BigDecimal("1.0"));
        properties.setInsuranceCostPercent(new BigDecimal("2.0"));

        offerService = new LoanOfferService(properties);
    }

    @Test
    @DisplayName("Успешное создание 4 кредитных предложений при корректных данных")
    void whenClientDataIsValidThenCalculateLoanOffersReturnsFourOffers() {
        List<LoanOfferDto> offers = offerService.calculateLoanOffers(request);

        assertNotNull(offers);
        assertEquals(4, offers.size(), "Должно быть создано ровно 4 предложения");
    }

    @Test
    @DisplayName("Процентная ставка по кредиту должна отличаться по застрахованным и незастрахованным кредитам на указанную величину (INSURANCE_RATE_DISCOUNT)")
    void loanInterestRateShouldBeDifferentForInsuredAndUninsuredLoans(){
        BigDecimal rateIsInsuranceClient = offerService.calculateLoanOffers(request).stream()
                .filter(offer -> !offer.getIsSalaryClient() && offer.getIsInsuranceEnabled())
                .findFirst().get().getRate();

        BigDecimal rateIsNotInsuranceClient = offerService.calculateLoanOffers(request).stream()
                .filter(offer -> !offer.getIsSalaryClient() && !offer.getIsInsuranceEnabled())
                .findFirst().get().getRate();

        assertTrue(rateIsInsuranceClient.compareTo(rateIsNotInsuranceClient) < 0);
        assertEquals(INSURANCE_RATE_DISCOUNT,
                rateIsNotInsuranceClient.subtract(rateIsInsuranceClient).setScale(RESULT_SCALE, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Процентная ставка по кредиту должна отличаться у зарплатного и не зарплатного клиента на указанную величину (SALARY_CLIENT_DISCOUNT)")
    void theRateOfAnSalaryClientMustBeHigherThanThatOfAnSalary(){
        BigDecimal rateIsSalaryClient = offerService.calculateLoanOffers(request).stream()
                .filter(offer -> !offer.getIsInsuranceEnabled() && offer.getIsSalaryClient())
                .findFirst().get().getRate();

        BigDecimal rateIsNotSalaryClient = offerService.calculateLoanOffers(request).stream()
                .filter(offer -> !offer.getIsInsuranceEnabled() && !offer.getIsSalaryClient())
                .findFirst().get().getRate();

        assertTrue(rateIsSalaryClient.compareTo(rateIsNotSalaryClient) < 0);
        assertEquals(SALARY_CLIENT_DISCOUNT,
                rateIsNotSalaryClient.subtract(rateIsSalaryClient).setScale(RESULT_SCALE, RoundingMode.HALF_UP));
    }
}