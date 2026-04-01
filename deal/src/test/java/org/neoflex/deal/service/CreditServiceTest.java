package org.neoflex.deal.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neoflex.deal.client.calculator.CalculatorClientService;
import org.neoflex.deal.dto.CreditDto;
import org.neoflex.deal.dto.EmploymentDto;
import org.neoflex.deal.dto.FinishRegistrationRequestDto;
import org.neoflex.deal.dto.PaymentScheduleElementDto;
import org.neoflex.deal.dto.ScoringDataDto;
import org.neoflex.deal.mapper.ClientMapper;
import org.neoflex.deal.mapper.CreditMapper;
import org.neoflex.deal.model.Credit;
import org.neoflex.deal.model.Statement;
import org.neoflex.deal.model.enums.ApplicationStatus;
import org.neoflex.deal.model.enums.ChangeType;
import org.neoflex.deal.model.enums.CreditStatus;
import org.neoflex.deal.model.enums.Gender;
import org.neoflex.deal.model.enums.MaritalStatus;
import org.neoflex.deal.model.jsonb.StatusHistory;
import org.neoflex.deal.repository.CreditRepository;
import org.neoflex.deal.repository.StatementRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Тесты сервиса CreditService")
@ExtendWith(MockitoExtension.class)
class CreditServiceTest {

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private CalculatorClientService calculatorClientService;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private CreditMapper creditMapper;

    @InjectMocks
    private CreditService creditService;

    private FinishRegistrationRequestDto finishRequest;
    private Statement testStatement;
    private CreditDto creditDto;
    private Credit credit;
    private UUID statementId;
    private ScoringDataDto scoringDataDto;

    @BeforeEach
    void setUp() {
        statementId = UUID.randomUUID();

        finishRequest = FinishRegistrationRequestDto.builder()
                .gender(Gender.MALE)
                .maritalStatus(MaritalStatus.MARRIED)
                .dependentAmount(2)
                .passportIssueDate(LocalDate.of(2010, 5, 15))
                .passportIssueBranch("123-456")
                .employment(EmploymentDto.builder()
                        .employerInn("1234567890")
                        .salary(BigDecimal.valueOf(100000))
                        .position(org.neoflex.deal.model.enums.Position.SPECIALIST)
                        .workExperienceTotal(60)
                        .workExperienceCurrent(24)
                        .build())
                .accountNumber("40817810000000000001")
                .build();

        testStatement = Statement.builder()
                .statementId(statementId)
                .status(ApplicationStatus.PREAPPROVAL)
                .statusHistory(new ArrayList<>())
                .creationDate(LocalDateTime.now())
                .build();

        List<PaymentScheduleElementDto> paymentSchedule = List.of(
                PaymentScheduleElementDto.builder()
                        .number(1)
                        .date(LocalDate.now().plusMonths(1))
                        .totalPayment(BigDecimal.valueOf(88848.89))
                        .principalPayment(BigDecimal.valueOf(78848.89))
                        .interestPayment(BigDecimal.valueOf(10000.00))
                        .remainingDebt(BigDecimal.valueOf(921151.11))
                        .build()
        );

        creditDto = CreditDto.builder()
                .amount(BigDecimal.valueOf(1000000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(88848.89))
                .rate(BigDecimal.valueOf(15.0))
                .psk(BigDecimal.valueOf(1066186.68))
                .isInsuranceEnabled(true)
                .isSalaryClient(true)
                .paymentSchedule(paymentSchedule)
                .build();

        credit = new Credit();
        credit.setCreditId(UUID.randomUUID());
        credit.setAmount(BigDecimal.valueOf(1000000));
        credit.setTerm(12);
        credit.setMonthlyPayment(BigDecimal.valueOf(88848.89));
        credit.setRate(BigDecimal.valueOf(15.0));
        credit.setPsk(BigDecimal.valueOf(1066186.68));
        credit.setIsInsuranceEnabled(true);
        credit.setIsSalaryClient(true);
        credit.setPaymentSchedule(paymentSchedule);
        credit.setCreditStatus(CreditStatus.CALCULATED);

        scoringDataDto = ScoringDataDto.builder()
                .amount(BigDecimal.valueOf(1000000))
                .term(12)
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1990, 1, 1))
                .passportSeries("1234")
                .passportNumber("567890")
                .passportIssueDate(LocalDate.of(2010, 5, 15))
                .passportIssueBranch("123-456")
                .maritalStatus(MaritalStatus.MARRIED)
                .dependentAmount(2)
                .employment(org.neoflex.deal.dto.EmploymentDto.builder()
                        .employerInn("1234567890")
                        .salary(BigDecimal.valueOf(100000))
                        .position(org.neoflex.deal.model.enums.Position.SPECIALIST)
                        .workExperienceTotal(60)
                        .workExperienceCurrent(24)
                        .build())
                .accountNumber("40817810000000000001")
                .isInsuranceEnabled(true)
                .isSalaryClient(true)
                .build();
    }

    @Test
    @DisplayName("Успешное завершение регистрации и расчет кредита при корректных данных")
    void whenValidRequestThenCalculateCreditSuccessfully() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCreditDto(creditDto)).thenReturn(credit);
        when(creditRepository.save(any(Credit.class))).thenReturn(credit);
        when(statementRepository.save(any(Statement.class))).thenReturn(testStatement);

        creditService.completionOfRegistrationAndFullCreditCalculation(finishRequest, statementId);

        verify(statementRepository, times(1)).findById(statementId);
        verify(clientMapper, times(1)).toScoringDataDto(finishRequest, testStatement);
        verify(calculatorClientService, times(1)).calculateCredit(scoringDataDto);
        verify(creditMapper, times(1)).toCreditDto(creditDto);
        verify(creditRepository, times(1)).save(any(Credit.class));
        verify(statementRepository, times(1)).save(testStatement);
    }

    @Test
    @DisplayName("При null запросе выбрасывается NullPointerException")
    void whenFinishRequestIsNullThenThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> creditService.completionOfRegistrationAndFullCreditCalculation(null, statementId));
    }

    @Test
    @DisplayName("При несуществующем statementId выбрасывается EntityNotFoundException")
    void whenStatementNotFoundThenThrowEntityNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();
        when(statementRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> creditService.completionOfRegistrationAndFullCreditCalculation(finishRequest, nonExistentId));

        assertTrue(exception.getMessage().contains(nonExistentId.toString()));
        verify(creditRepository, never()).save(any(Credit.class));
        verify(statementRepository, never()).save(any(Statement.class));
    }

    @Test
    @DisplayName("При сохранении кредита выбрасывается исключение")
    void whenSavingCreditFailsThenThrowException() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCreditDto(creditDto)).thenReturn(credit);
        when(creditRepository.save(any(Credit.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class,
                () -> creditService.completionOfRegistrationAndFullCreditCalculation(finishRequest, statementId));

        verify(statementRepository, times(1)).findById(statementId);
        verify(creditRepository, times(1)).save(any(Credit.class));
        verify(statementRepository, never()).save(any(Statement.class));
    }

    @Test
    @DisplayName("При успешном расчете статус заявки меняется на APPROVED")
    void whenCreditCalculatedThenStatementStatusChangesToApproved() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCreditDto(creditDto)).thenReturn(credit);
        when(creditRepository.save(any(Credit.class))).thenReturn(credit);
        when(statementRepository.save(any(Statement.class))).thenReturn(testStatement);

        assertEquals(ApplicationStatus.PREAPPROVAL, testStatement.getStatus());

        creditService.completionOfRegistrationAndFullCreditCalculation(finishRequest, statementId);

        assertEquals(ApplicationStatus.APPROVED, testStatement.getStatus());
    }

    @Test
    @DisplayName("При успешном расчете в историю статусов добавляется запись")
    void whenCreditCalculatedThenStatusHistoryContainsNewRecord() {
        testStatement.setStatusHistory(new ArrayList<>());

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCreditDto(creditDto)).thenReturn(credit);
        when(creditRepository.save(any(Credit.class))).thenReturn(credit);
        when(statementRepository.save(any(Statement.class))).thenReturn(testStatement);

        assertEquals(0, testStatement.getStatusHistory().size());

        creditService.completionOfRegistrationAndFullCreditCalculation(finishRequest, statementId);

        assertEquals(1, testStatement.getStatusHistory().size());

        StatusHistory history = testStatement.getStatusHistory().get(0);
        assertEquals(ApplicationStatus.APPROVED, history.getStatus());
        assertEquals(ChangeType.AUTOMATIC, history.getChangeType());
        assertNotNull(history.getTime());
    }

    @Test
    @DisplayName("При успешном расчете кредит сохраняется со статусом CALCULATED")
    void whenCreditCalculatedThenCreditHasCalculatedStatus() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCreditDto(creditDto)).thenReturn(credit);
        when(creditRepository.save(any(Credit.class))).thenReturn(credit);
        when(statementRepository.save(any(Statement.class))).thenReturn(testStatement);

        creditService.completionOfRegistrationAndFullCreditCalculation(finishRequest, statementId);

        verify(creditRepository, times(1)).save(any(Credit.class));
        assertEquals(CreditStatus.CALCULATED, credit.getCreditStatus());
    }

    @Test
    @DisplayName("При успешном расчете кредит содержит все данные из CreditDto")
    void whenCreditCalculatedThenCreditContainsAllDataFromCreditDto() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCreditDto(creditDto)).thenReturn(credit);
        when(creditRepository.save(any(Credit.class))).thenReturn(credit);
        when(statementRepository.save(any(Statement.class))).thenReturn(testStatement);

        creditService.completionOfRegistrationAndFullCreditCalculation(finishRequest, statementId);

        assertEquals(creditDto.getAmount(), credit.getAmount());
        assertEquals(creditDto.getTerm(), credit.getTerm());
        assertEquals(creditDto.getMonthlyPayment(), credit.getMonthlyPayment());
        assertEquals(creditDto.getRate(), credit.getRate());
        assertEquals(creditDto.getPsk(), credit.getPsk());
        assertEquals(creditDto.getIsInsuranceEnabled(), credit.getIsInsuranceEnabled());
        assertEquals(creditDto.getIsSalaryClient(), credit.getIsSalaryClient());
        assertEquals(creditDto.getPaymentSchedule(), credit.getPaymentSchedule());
    }

    @Test
    @DisplayName("При вызове calculatorClientService.calculateCredit передается корректный ScoringDataDto")
    void whenCreditCalculatedThenScoringDataDtoContainsCorrectData() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCreditDto(creditDto)).thenReturn(credit);
        when(creditRepository.save(any(Credit.class))).thenReturn(credit);
        when(statementRepository.save(any(Statement.class))).thenReturn(testStatement);

        creditService.completionOfRegistrationAndFullCreditCalculation(finishRequest, statementId);

        verify(calculatorClientService, times(1)).calculateCredit(scoringDataDto);
    }

    @Test
    @DisplayName("При вызове clientMapper.toScoringDataDto передаются корректные параметры")
    void whenCreditCalculatedThenClientMapperReceivesCorrectParameters() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCreditDto(creditDto)).thenReturn(credit);
        when(creditRepository.save(any(Credit.class))).thenReturn(credit);
        when(statementRepository.save(any(Statement.class))).thenReturn(testStatement);

        creditService.completionOfRegistrationAndFullCreditCalculation(finishRequest, statementId);

        verify(clientMapper, times(1)).toScoringDataDto(finishRequest, testStatement);
    }

    @Test
    @DisplayName("При успешном расчете statement сохраняется в репозитории")
    void whenCreditCalculatedThenStatementIsSaved() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCreditDto(creditDto)).thenReturn(credit);
        when(creditRepository.save(any(Credit.class))).thenReturn(credit);
        when(statementRepository.save(any(Statement.class))).thenReturn(testStatement);

        creditService.completionOfRegistrationAndFullCreditCalculation(finishRequest, statementId);

        verify(statementRepository, times(1)).save(testStatement);
    }

    @Test
    @DisplayName("При успешном расчете в заявку сохраняется кредит")
    void whenCreditCalculatedThenStatementCreditIsSet() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCreditDto(creditDto)).thenReturn(credit);
        when(creditRepository.save(any(Credit.class))).thenReturn(credit);
        when(statementRepository.save(any(Statement.class))).thenReturn(testStatement);

        assertNull(testStatement.getCredit());

        creditService.completionOfRegistrationAndFullCreditCalculation(finishRequest, statementId);

        assertNotNull(testStatement.getCredit());
        assertEquals(credit.getCreditId(), testStatement.getCredit().getCreditId());
    }
}