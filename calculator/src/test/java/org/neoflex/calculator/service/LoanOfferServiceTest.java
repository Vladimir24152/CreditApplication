package org.neoflex.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neoflex.calculator.config.LoanCalculatorProperties;
import org.neoflex.calculator.dto.LoanStatementRequestDto;
import org.neoflex.calculator.exception.NotValidBirthDateException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование сервиса формирования кредитных предложений")
class LoanOfferServiceTest {

    @Mock
    private LoanCalculatorProperties properties;

    @InjectMocks
    private LoanOfferService offerService;

    private static final BigDecimal PERCENT_DIVISOR = new BigDecimal("100");
    private static final int CALC_SCALE = 5;
    private static final int RESULT_SCALE = 2;

    private static final BigDecimal BASE_RATE = new BigDecimal("15.00");
    private static final BigDecimal INSURANCE_COST_PERCENT = new BigDecimal("2.00");
    private static final BigDecimal INSURANCE_RATE_DISCOUNT = new BigDecimal("3.00");
    private static final BigDecimal SALARY_CLIENT_DISCOUNT = new BigDecimal("1.00");

    private final BigDecimal TEST_AMOUNT = new BigDecimal(1_000_000);
    private final Integer TEST_TERM = 12;
    private final String TEST_FIRST_NAME = "Ivan";
    private final String TEST_LASTNAME = "Ivanov";
    private final LocalDate TEST_BIRTH_DATE = LocalDate.now().minusYears(25);

    private LoanStatementRequestDto request;

    @BeforeEach
    void setUp(){
        request = LoanStatementRequestDto.builder()
                .amount(TEST_AMOUNT)
                .term(TEST_TERM)
                .firstName(TEST_FIRST_NAME)
                .lastName(TEST_LASTNAME)
                .birthDate(TEST_BIRTH_DATE)
                .build();

        lenient().when(properties.getBaseRate()).thenReturn(BASE_RATE);
        lenient().when(properties.getInsuranceCostPercent()).thenReturn(INSURANCE_COST_PERCENT);
        lenient().when(properties.getInsuranceRateDiscount()).thenReturn(INSURANCE_RATE_DISCOUNT);
        lenient().when(properties.getSalaryClientDiscount()).thenReturn(SALARY_CLIENT_DISCOUNT);
    }

    @Test
    @DisplayName("Если возраст клиента меньше 18 лет - должен выбросить NotValidBirthDateException")
    void whenTheClientIsUnder18YearsOfAgeThenTrowNotValidBirthDateException(){
        request.setBirthDate(LocalDate.now().minusYears(15));
        assertThrows(NotValidBirthDateException.class,() -> offerService.calculateLoanOffers(request));
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

    @Test
    @DisplayName("ПСК должна быть равна произведению месячного платежа на количество планируемых платежей")
    void pskMustBeEqualMonthlyPaymentMultiplyByTerm(){
        offerService.calculateLoanOffers(request)
                .forEach(offer -> {
                    assertEquals(
                            offer.getTotalAmount()
                                    .setScale(0, RoundingMode.HALF_UP),
                            offer.getMonthlyPayment()
                                    .multiply(new BigDecimal(offer.getTerm()))
                                    .setScale(0, RoundingMode.HALF_UP)
                    );
                });
    }
}