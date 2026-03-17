package org.neoflex.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.neoflex.calculator.config.LoanCalculatorProperties;
import org.neoflex.calculator.dto.response.CreditDto;
import org.neoflex.calculator.dto.EmploymentDto;
import org.neoflex.calculator.dto.ScoringDataDto;
import org.neoflex.calculator.enums.EmploymentStatus;
import org.neoflex.calculator.enums.Gender;
import org.neoflex.calculator.enums.MaritalStatus;
import org.neoflex.calculator.enums.Position;


import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты сервиса CreditCalculationService")
class CreditCalculationServiceTest {

    private CreditCalculationService creditCalculationService;
    private LoanCalculatorProperties properties;

    private ScoringDataDto request;

    EmploymentDto employment;

    @BeforeEach
    void setUp() {
        properties = new LoanCalculatorProperties();
        properties.setBaseRate(new BigDecimal("15.0"));
        properties.setInsuranceRateDiscount(new BigDecimal("3.0"));
        properties.setInsuranceCostPercent(new BigDecimal("2.0"));
        properties.setMarriedRateDiscount(new BigDecimal("3.0"));
        properties.setMaleRateDiscount(new BigDecimal("3.0"));
        properties.setMidManagerRateDiscount(new BigDecimal("2.0"));
        properties.setTopManagerRateDiscount(new BigDecimal("3.0"));
        properties.setSelfEmployRateAdd(new BigDecimal("2.0"));
        properties.setBusinessOwnerRateAdd(new BigDecimal("1.0"));
        properties.setDivorcedRateAdd(new BigDecimal("1.0"));
        properties.setNotBinaryRateAdd(new BigDecimal("7.0"));

        creditCalculationService = new CreditCalculationService(properties);

        employment = EmploymentDto.builder()
                .employmentStatus(EmploymentStatus.EMPLOYED)
                .employerInn("1234567890")
                .salary(new BigDecimal(100_000))
                .position(Position.SPECIALIST)
                .workExperienceTotal(60)
                .workExperienceCurrent(24)
                .build();

        request = ScoringDataDto.builder()
                .amount(new BigDecimal("1000000"))
                .term(12)
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
    }

    @Test
    @DisplayName("Успешный расчет кредита при корректных данных клиента")
    void whenClientDataIsValidThenCalculateCreditReturnsCreditDto() {

        CreditDto result = creditCalculationService.calculateCredit(request);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("1000000").compareTo(result.getAmount()));
        assertEquals(12, result.getTerm());
        assertNotNull(result.getPaymentSchedule());
        assertEquals(12, result.getPaymentSchedule().size(),
                "График платежей должен содержать 12 записей");
    }


    @Test
    @DisplayName("Общий стаж ровно 18 месяцев - исключение не выбрасывается")
    void whenTotalExperienceEquals18MonthsThenNoExceptionThrown() {
        request.getEmployment().setWorkExperienceTotal(18);

        assertDoesNotThrow(() -> creditCalculationService.calculateCredit(request));
    }

    @Test
    @DisplayName("Расчет ПСК: ПСК должен быть больше суммы кредита")
    void whenCreditCalculatedThenPskShouldBeGreaterThanAmount() {

        CreditDto result = creditCalculationService.calculateCredit(request);

        assertTrue(result.getPsk().compareTo(result.getAmount()) > 0);
    }

    @Test
    @DisplayName("Расчет графика платежей: остаток долга в последнем платеже должен быть 0")
    void whenPaymentScheduleCalculatedThenLastRemainingDebtShouldBeZero() {

        CreditDto result = creditCalculationService.calculateCredit(request);

        BigDecimal lastRemainingDebt = result.getPaymentSchedule()
                .get(result.getPaymentSchedule().size() - 1)
                .getRemainingDebt();

        assertEquals(0, BigDecimal.ZERO.compareTo(lastRemainingDebt));
    }

    @Test
    @DisplayName("Расчет графика платежей: первый платеж должен быть через месяц")
    void whenPaymentScheduleCalculatedThenFirstPaymentDateIsOneMonthLater() {
        LocalDate expectedFirstPaymentDate = LocalDate.now().plusMonths(1);

        CreditDto result = creditCalculationService.calculateCredit(request);

        assertEquals(expectedFirstPaymentDate, result.getPaymentSchedule().get(0).getDate());
    }

    @Test
    @DisplayName("Расчет для самозанятого - ставка увеличивается")
    void whenSelfEmployedThenRateIncreases() {
        request.getEmployment().setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);
        CreditDto selfEmployedCredit = creditCalculationService.calculateCredit(request);

        request.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);
        CreditDto employedCredit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = selfEmployedCredit.getRate().subtract(employedCredit.getRate());

        assertTrue(difference.compareTo(properties.getSelfEmployRateAdd()) == 0);
    }

    @Test
    @DisplayName("Расчет для владельца бизнеса - ставка увеличивается")
    void whenBusinessOwnerThenRateIncreases() {
        request.getEmployment().setEmploymentStatus(EmploymentStatus.BUSINESS_OWNER);
        CreditDto businessOwnerCredit = creditCalculationService.calculateCredit(request);

        request.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);
        CreditDto employedCredit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = businessOwnerCredit.getRate().subtract(employedCredit.getRate());

        assertTrue(difference.compareTo(properties.getBusinessOwnerRateAdd()) == 0);
    }

    @Test
    @DisplayName("Расчет для топ-менеджера - ставка уменьшается")
    void whenTopManagerThenRateDecreases() {
        request.getEmployment().setPosition(Position.TOP_MANAGER);
        CreditDto topManagerCredit = creditCalculationService.calculateCredit(request);

        request.getEmployment().setPosition(Position.SPECIALIST);
        CreditDto credit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = credit.getRate().subtract(topManagerCredit.getRate());

        assertTrue(difference.compareTo(properties.getTopManagerRateDiscount()) == 0);
    }

    @Test
    @DisplayName("Расчет для мидл-менеджера - ставка уменьшается")
    void whenMidManagerThenRateDecreases() {
        request.getEmployment().setPosition(Position.MID_MANAGER);
        CreditDto midManagerCredit = creditCalculationService.calculateCredit(request);

        request.getEmployment().setPosition(Position.SPECIALIST);
        CreditDto credit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = credit.getRate().subtract(midManagerCredit.getRate());

        assertTrue(difference.compareTo(properties.getMidManagerRateDiscount()) == 0);
    }

    @Test
    @DisplayName("Расчет для женатого клиента - ставка уменьшается")
    void whenMarriedThenRateDecreases() {
        request.setMaritalStatus(MaritalStatus.MARRIED);
        CreditDto marriedCredit = creditCalculationService.calculateCredit(request);

        request.setMaritalStatus(MaritalStatus.SINGLE);
        CreditDto credit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = credit.getRate().subtract(marriedCredit.getRate());

        assertTrue(difference.compareTo(properties.getMarriedRateDiscount()) == 0);
    }

    @Test
    @DisplayName("Расчет для разведенного клиента - ставка увеличивается")
    void whenDivorcedThenRateIncreases() {
        request.setMaritalStatus(MaritalStatus.DIVORCED);
        CreditDto divorcedCredit = creditCalculationService.calculateCredit(request);

        request.setMaritalStatus(MaritalStatus.SINGLE);
        CreditDto credit = creditCalculationService.calculateCredit(request);

        BigDecimal difference = divorcedCredit.getRate().subtract(credit.getRate());

        assertTrue(difference.compareTo(properties.getDivorcedRateAdd()) == 0);
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

        assertTrue(difference.compareTo(properties.getMaleRateDiscount()) == 0);
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

        assertTrue(difference.compareTo(BigDecimal.ZERO) == 0);
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

        assertTrue(difference.compareTo(BigDecimal.ZERO) == 0);
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

        assertTrue(difference.compareTo(properties.getNotBinaryRateAdd()) == 0);
    }
}