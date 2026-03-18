package org.neoflex.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.neoflex.calculator.constants.LoanCalculatorConstants;
import org.neoflex.calculator.dto.LoanStatementRequestDto;
import org.neoflex.calculator.dto.response.LoanOfferDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Тестирование сервиса формирования кредитных предложений")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoanOfferServiceTest {

    @Mock
    private CreditCalculator creditCalculator;

    @InjectMocks
    private LoanOfferService offerService;

    private static final BigDecimal EXPECTED_AMOUNT = new BigDecimal("1000000");
    private static final int EXPECTED_TERM = 12;

    private LoanStatementRequestDto request;

    @BeforeEach
    void setUp(){
        request = LoanStatementRequestDto.builder()
                .amount(EXPECTED_AMOUNT)
                .term(EXPECTED_TERM)
                .firstName("Ivan")
                .lastName("Ivanov")
                .birthDate(LocalDate.now().minusYears(25))
                .build();


        when(creditCalculator.calculateMonthlyPayment(any(), anyInt(), any()))
                .thenReturn(new BigDecimal("90258.31"));

    }

    @Test
    @DisplayName("Успешное создание 4 кредитных предложений при корректных данных")
    void whenClientDataIsValidThenCalculateLoanOffersReturnsFourOffers() {
        List<LoanOfferDto> offers = offerService.calculateLoanOffers(request);

        assertNotNull(offers);
        assertEquals(4, offers.size(), "Должно быть создано ровно 4 предложения");
        verify(creditCalculator, times(4)).calculateMonthlyPayment(
                eq(EXPECTED_AMOUNT),
                eq(EXPECTED_TERM),
                any(BigDecimal.class)
        );
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
        assertEquals(LoanCalculatorConstants.INSURANCE_RATE_DISCOUNT,
                rateIsNotInsuranceClient.subtract(rateIsInsuranceClient).setScale(1, RoundingMode.HALF_UP));

        verify(creditCalculator, times(8)).calculateMonthlyPayment(
                eq(EXPECTED_AMOUNT),
                eq(EXPECTED_TERM),
                any(BigDecimal.class)
        );
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
        assertEquals(LoanCalculatorConstants.SALARY_CLIENT_DISCOUNT,
                rateIsNotSalaryClient.subtract(rateIsSalaryClient).setScale(1, RoundingMode.HALF_UP));

        verify(creditCalculator, times(8)).calculateMonthlyPayment(
                eq(EXPECTED_AMOUNT),
                eq(EXPECTED_TERM),
                any(BigDecimal.class)
        );
    }

    @Test
    @DisplayName("Должен бросить NPE если request == null")
    void whenRequestIsNullThenReturnNPE() {
        request = null;

        assertThrows(NullPointerException.class,() -> offerService.calculateLoanOffers(request));
    }
}