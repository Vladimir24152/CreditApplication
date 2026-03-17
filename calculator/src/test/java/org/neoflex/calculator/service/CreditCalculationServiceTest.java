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
import org.neoflex.calculator.dto.EmploymentDto;
import org.neoflex.calculator.dto.ScoringDataDto;
import org.neoflex.calculator.dto.response.CreditDto;
import org.neoflex.calculator.enums.EmploymentStatus;
import org.neoflex.calculator.enums.Gender;
import org.neoflex.calculator.enums.MaritalStatus;
import org.neoflex.calculator.enums.Position;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Тесты сервиса CreditCalculationService")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreditCalculationServiceTest {

    @Mock
    private CreditCalculator creditCalculator;

    @InjectMocks
    private CreditCalculationService creditCalculationService;

    private ScoringDataDto request;

    private static final BigDecimal EXPECTED_AMOUNT = new BigDecimal("1000000");
    private static final int EXPECTED_TERM = 12;


    @BeforeEach
    void setUp() {

        EmploymentDto employment = EmploymentDto.builder()
                .employmentStatus(EmploymentStatus.EMPLOYED)
                .employerInn("1234567890")
                .salary(new BigDecimal(100_000))
                .position(Position.SPECIALIST)
                .workExperienceTotal(60)
                .workExperienceCurrent(24)
                .build();

        request = ScoringDataDto.builder()
                .amount(EXPECTED_AMOUNT)
                .term(EXPECTED_TERM)
                .firstName("Иван")
                .lastName("Иванов")
                .middleName("Иванович")
                .birthDate(LocalDate.now().minusYears(30))
                .passportSeries("1234")
                .passportNumber("567890")
                .passportIssueDate(LocalDate.now().minusYears(5))
                .passportIssueBranch("ОВД")
                .maritalStatus(MaritalStatus.MARRIED)
                .dependentAmount(2)
                .employment(employment)
                .accountNumber("40817810000000000001")
                .isInsuranceEnabled(true)
                .isSalaryClient(true)
                .gender(Gender.MALE)
                .build();

        when(creditCalculator.calculateMonthlyPayment(any(), anyInt(), any()))
                .thenReturn(new BigDecimal("90258.31"));
        when(creditCalculator.calculateMonthlyRate(any()))
                .thenReturn(new BigDecimal("0.0125"));
    }

    @Test
    @DisplayName("Успешный расчет кредита при корректных данных клиента")
    void whenClientDataIsValidThenCalculateCreditReturnsCreditDto() {


        CreditDto result = creditCalculationService.calculateCredit(request);

        assertNotNull(result);
        assertEquals(0, EXPECTED_AMOUNT.compareTo(result.getAmount()));
        assertEquals(EXPECTED_TERM, result.getTerm());
        assertNotNull(result.getPaymentSchedule());
        assertEquals(EXPECTED_TERM, result.getPaymentSchedule().size(),
                "График платежей должен содержать 12 записей");

        verify(creditCalculator, times(1)).calculateMonthlyPayment(
                eq(EXPECTED_AMOUNT),
                eq(EXPECTED_TERM),
                any(BigDecimal.class)
        );
        verify(creditCalculator).calculateMonthlyRate(any());
    }


    @Test
    @DisplayName("Общий стаж ровно 18 месяцев - исключение не выбрасывается")
    void whenTotalExperienceEquals18MonthsThenNoExceptionThrown() {

        request.getEmployment().setWorkExperienceTotal(18);

        assertDoesNotThrow(() -> creditCalculationService.calculateCredit(request));

        verify(creditCalculator, times(1)).calculateMonthlyPayment(
                eq(EXPECTED_AMOUNT),
                eq(EXPECTED_TERM),
                any(BigDecimal.class)
        );
        verify(creditCalculator).calculateMonthlyRate(any());
    }

    @Test
    @DisplayName("Расчет ПСК: ПСК должен быть больше суммы кредита")
    void whenCreditCalculatedThenPskShouldBeGreaterThanAmount() {

        CreditDto result = creditCalculationService.calculateCredit(request);

        assertTrue(result.getPsk().compareTo(result.getAmount()) > 0);

        verify(creditCalculator, times(1)).calculateMonthlyPayment(
                eq(EXPECTED_AMOUNT),
                eq(12),
                any(BigDecimal.class)
        );
        verify(creditCalculator).calculateMonthlyRate(any());
    }

    @Test
    @DisplayName("Расчет графика платежей: остаток долга в последнем платеже должен быть 0")
    void whenPaymentScheduleCalculatedThenLastRemainingDebtShouldBeZero() {

        CreditDto result = creditCalculationService.calculateCredit(request);

        BigDecimal lastRemainingDebt = result.getPaymentSchedule()
                .get(result.getPaymentSchedule().size() - 1)
                .getRemainingDebt();

        assertEquals(0, BigDecimal.ZERO.compareTo(lastRemainingDebt));

        verify(creditCalculator, times(1)).calculateMonthlyPayment(
                eq(EXPECTED_AMOUNT),
                eq(12),
                any(BigDecimal.class)
        );
        verify(creditCalculator).calculateMonthlyRate(any());
    }

    @Test
    @DisplayName("Расчет графика платежей: первый платеж должен быть через месяц")
    void whenPaymentScheduleCalculatedThenFirstPaymentDateIsOneMonthLater() {
        LocalDate expectedFirstPaymentDate = LocalDate.now().plusMonths(1);

        CreditDto result = creditCalculationService.calculateCredit(request);

        assertEquals(expectedFirstPaymentDate, result.getPaymentSchedule().get(0).getDate());

        verify(creditCalculator, times(1)).calculateMonthlyPayment(
                eq(EXPECTED_AMOUNT),
                eq(EXPECTED_TERM),
                any(BigDecimal.class)
        );
        verify(creditCalculator).calculateMonthlyRate(any());
    }

    @Test
    @DisplayName("Расчет для самозанятого - ставка увеличивается")
    void whenSelfEmployedThenRateIncreases() {
        request.getEmployment().setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);
        CreditDto selfEmployedCredit = creditCalculationService.calculateCredit(request);

        request.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);
        CreditDto employedCredit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = selfEmployedCredit.getRate().subtract(employedCredit.getRate());

        assertEquals(0,difference.compareTo(LoanCalculatorConstants.SELF_EMPLOY_RATE_ADD));
        verify(creditCalculator,times(2)).calculateMonthlyRate(any());
    }

    @Test
    @DisplayName("Расчет для владельца бизнеса - ставка увеличивается")
    void whenBusinessOwnerThenRateIncreases() {
        request.getEmployment().setEmploymentStatus(EmploymentStatus.BUSINESS_OWNER);
        CreditDto businessOwnerCredit = creditCalculationService.calculateCredit(request);

        request.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);
        CreditDto employedCredit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = businessOwnerCredit.getRate().subtract(employedCredit.getRate());

        assertEquals(0, difference.compareTo(LoanCalculatorConstants.BUSINESS_OWNER_RATE_ADD));
        verify(creditCalculator,times(2)).calculateMonthlyRate(any());
    }

    @Test
    @DisplayName("Расчет для топ-менеджера - ставка уменьшается")
    void whenTopManagerThenRateDecreases() {
        request.getEmployment().setPosition(Position.TOP_MANAGER);
        CreditDto topManagerCredit = creditCalculationService.calculateCredit(request);

        request.getEmployment().setPosition(Position.SPECIALIST);
        CreditDto credit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = credit.getRate().subtract(topManagerCredit.getRate());

        assertEquals(0,difference.compareTo(LoanCalculatorConstants.TOP_MANAGER_RATE_DISCOUNT));
        verify(creditCalculator,times(2)).calculateMonthlyRate(any());
    }

    @Test
    @DisplayName("Расчет для мидл-менеджера - ставка уменьшается")
    void whenMidManagerThenRateDecreases() {
        request.getEmployment().setPosition(Position.MID_MANAGER);
        CreditDto midManagerCredit = creditCalculationService.calculateCredit(request);

        request.getEmployment().setPosition(Position.SPECIALIST);
        CreditDto credit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = credit.getRate().subtract(midManagerCredit.getRate());

        assertEquals(0, difference.compareTo(LoanCalculatorConstants.MID_MANAGER_RATE_DISCOUNT));
        verify(creditCalculator,times(2)).calculateMonthlyRate(any());
    }

    @Test
    @DisplayName("Расчет для женатого клиента - ставка уменьшается")
    void whenMarriedThenRateDecreases() {
        request.setMaritalStatus(MaritalStatus.MARRIED);
        CreditDto marriedCredit = creditCalculationService.calculateCredit(request);

        request.setMaritalStatus(MaritalStatus.SINGLE);
        CreditDto credit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = credit.getRate().subtract(marriedCredit.getRate());

        assertEquals(0,difference.compareTo(LoanCalculatorConstants.MARRIED_RATE_DISCOUNT));
        verify(creditCalculator,times(2)).calculateMonthlyRate(any());
    }

    @Test
    @DisplayName("Расчет для разведенного клиента - ставка увеличивается")
    void whenDivorcedThenRateIncreases() {
        request.setMaritalStatus(MaritalStatus.DIVORCED);
        CreditDto divorcedCredit = creditCalculationService.calculateCredit(request);

        request.setMaritalStatus(MaritalStatus.SINGLE);
        CreditDto credit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = divorcedCredit.getRate().subtract(credit.getRate());

        assertEquals(0, difference.compareTo(LoanCalculatorConstants.DIVORCED_RATE_ADD));
        verify(creditCalculator,times(2)).calculateMonthlyRate(any());
    }

    @Test
    @DisplayName("Расчет для мужчины в возрасте 30-55 лет - ставка уменьшается")
    void whenMaleAgeBetween30And55ThenRateDecreases() {
        request.setGender(Gender.MALE);
        request.setBirthDate(LocalDate.now().minusYears(40));
        CreditDto maleCredit = creditCalculationService.calculateCredit(request);

        request.setGender(Gender.MALE);
        request.setBirthDate(LocalDate.now().minusYears(20));
        CreditDto credit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = credit.getRate().subtract(maleCredit.getRate());

        assertEquals(0, difference.compareTo(LoanCalculatorConstants.MALE_RATE_DISCOUNT));
        verify(creditCalculator,times(2)).calculateMonthlyRate(any());
    }

    @Test
    @DisplayName("Расчет для мужчины младше 30 лет - ставка не уменьшается")
    void whenMaleAgeUnder30ThenRateDoesNotDecrease() {
        request.setGender(Gender.MALE);
        request.setBirthDate(LocalDate.now().minusYears(25));
        CreditDto maleCredit = creditCalculationService.calculateCredit(request);

        request.setGender(Gender.MALE);
        request.setBirthDate(LocalDate.now().minusYears(20));
        CreditDto credit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = credit.getRate().subtract(maleCredit.getRate());

        assertEquals(0, difference.compareTo(BigDecimal.ZERO));
        verify(creditCalculator,times(2)).calculateMonthlyRate(any());
    }

    @Test
    @DisplayName("Расчет для мужчины старше 55 лет - ставка не уменьшается")
    void whenMaleAgeOver55ThenRateDoesNotDecrease() {
        request.setGender(Gender.MALE);
        request.setBirthDate(LocalDate.now().minusYears(60));
        CreditDto maleCredit = creditCalculationService.calculateCredit(request);

        request.setGender(Gender.MALE);
        request.setBirthDate(LocalDate.now().minusYears(20));
        CreditDto credit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = credit.getRate().subtract(maleCredit.getRate());

        assertEquals(0, difference.compareTo(BigDecimal.ZERO));
        verify(creditCalculator,times(2)).calculateMonthlyRate(any());
    }

    @Test
    @DisplayName("Расчет для небинарного клиента - ставка увеличивается")
    void whenNotBinaryThenRateIncreases() {
        request.setGender(Gender.NOT_BINARY);
        CreditDto notBinaryCredit = creditCalculationService.calculateCredit(request);

        request.setGender(Gender.MALE);
        request.setBirthDate(LocalDate.now().minusYears(20));
        CreditDto credit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = notBinaryCredit.getRate().subtract(credit.getRate());

        assertEquals(0, difference.compareTo(LoanCalculatorConstants.NOT_BINARY_RATE_ADD));
        verify(creditCalculator,times(2)).calculateMonthlyRate(any());
    }

    @Test
    @DisplayName("Расчет для разведенного мужчины 40 лет со страховкой")
    void whenDivorcedMale40WithInsuranceThenMultipleAdjustments() {
        request.setMaritalStatus(MaritalStatus.DIVORCED);
        request.setGender(Gender.MALE);
        request.setBirthDate(LocalDate.now().minusYears(40));
        request.setIsInsuranceEnabled(true);

        CreditDto result = creditCalculationService.calculateCredit(request);

        assertNotNull(result);
        verify(creditCalculator, times(1)).calculateMonthlyPayment(
                eq(EXPECTED_AMOUNT),
                eq(EXPECTED_TERM),
                argThat(rate -> {
                    BigDecimal expectedRate = LoanCalculatorConstants.BASE_RATE
                            .add(LoanCalculatorConstants.DIVORCED_RATE_ADD)
                            .subtract(LoanCalculatorConstants.MALE_RATE_DISCOUNT)
                            .subtract(LoanCalculatorConstants.INSURANCE_RATE_DISCOUNT);
                    return rate.compareTo(expectedRate) == 0;
                })
        );
    }

    @Test
    @DisplayName("Расчет для женщины 35 лет без страховки")
    void whenFemale35WithoutInsuranceThenGenderDiscountApplied() {
        request.setGender(Gender.FEMALE);
        request.setBirthDate(LocalDate.now().minusYears(35));
        request.setIsInsuranceEnabled(false);

        CreditDto result = creditCalculationService.calculateCredit(request);

        assertNotNull(result);
        verify(creditCalculator, times(1)).calculateMonthlyPayment(
                eq(EXPECTED_AMOUNT),
                eq(EXPECTED_TERM),
                argThat(rate -> {
                    BigDecimal expectedRate = LoanCalculatorConstants.BASE_RATE
                            .subtract(LoanCalculatorConstants.MARRIED_RATE_DISCOUNT)
                            .subtract(LoanCalculatorConstants.FEMALE_RATE_DISCOUNT);
                    return rate.compareTo(expectedRate) == 0;
                })
        );
    }

    @Test
    @DisplayName("Расчет для небинарного клиента 40 лет со страховкой")
    void whenDivorcedNotBinary40WithInsuranceThenMultipleAdjustments() {
        request.setMaritalStatus(MaritalStatus.DIVORCED);
        request.setGender(Gender.NOT_BINARY);
        request.setBirthDate(LocalDate.now().minusYears(40));
        request.setIsInsuranceEnabled(true);

        CreditDto result = creditCalculationService.calculateCredit(request);

        assertNotNull(result);
        verify(creditCalculator, times(1)).calculateMonthlyPayment(
                eq(EXPECTED_AMOUNT),
                eq(EXPECTED_TERM),
                argThat(rate -> {
                    BigDecimal expectedRate = LoanCalculatorConstants.BASE_RATE
                            .add(LoanCalculatorConstants.DIVORCED_RATE_ADD)
                            .add(LoanCalculatorConstants.NOT_BINARY_RATE_ADD)
                            .subtract(LoanCalculatorConstants.INSURANCE_RATE_DISCOUNT);
                    return rate.compareTo(expectedRate) == 0;
                })
        );
    }

    @Test
    @DisplayName("Должен бросить NPE если request == null")
    void whenRequestIsNullThenReturnNPE() {
        request = null;

        assertThrows(NullPointerException.class,() -> creditCalculationService.calculateCredit(request));
    }

    @Test
    @DisplayName("Должен бросить NPE если IsInsuranceEnabled == null")
    void whenIsInsuranceEnabledIsNullThenReturnNPE() {
        request.setIsInsuranceEnabled(null);

        assertThrows(NullPointerException.class,() -> creditCalculationService.calculateCredit(request));
    }
}