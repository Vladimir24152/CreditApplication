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
import org.neoflex.deal.model.Client;
import org.neoflex.deal.model.Credit;
import org.neoflex.deal.model.Statement;
import org.neoflex.deal.model.enums.ApplicationStatus;
import org.neoflex.deal.model.enums.ChangeType;
import org.neoflex.deal.model.enums.CreditStatus;
import org.neoflex.deal.model.enums.Gender;
import org.neoflex.deal.model.enums.MaritalStatus;
import org.neoflex.deal.model.jsonb.Passport;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @Mock
    private ClientService clientService;

    @InjectMocks
    private CreditService creditService;

    private FinishRegistrationRequestDto finishRequest;
    private Statement testStatement;
    private CreditDto creditDto;
    private Credit credit;
    private Client client;
    private UUID statementId;
    private UUID clientId;
    private ScoringDataDto scoringDataDto;

    @BeforeEach
    void setUp() {
        statementId = UUID.randomUUID();
        clientId = UUID.randomUUID();

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

        client = Client.builder()
                .clientId(clientId)
                .lastName("Ivanov")
                .firstName("Ivan")
                .middleName("Ivanovich")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("ivan@example.com")
                .gender(Gender.MALE)
                .maritalStatus(MaritalStatus.MARRIED)
                .dependentAmount(2)
                .passport(Passport.builder()
                        .series("1234")
                        .number("567890")
                        .issueBranch(null)
                        .issueDate(null)
                        .build())
                .accountNumber("40817810000000000001")
                .build();

        testStatement = Statement.builder()
                .statementId(statementId)
                .client(client)
                .status(ApplicationStatus.APPROVED)
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

        credit = Credit.builder()
                .creditId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(1000000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(88848.89))
                .rate(BigDecimal.valueOf(15.0))
                .psk(BigDecimal.valueOf(1066186.68))
                .isInsuranceEnabled(true)
                .isSalaryClient(true)
                .paymentSchedule(paymentSchedule)
                .creditStatus(CreditStatus.CALCULATED)
                .build();

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
                .employment(EmploymentDto.builder()
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
        when(creditMapper.toCredit(creditDto, CreditStatus.CALCULATED)).thenReturn(credit);
        when(creditRepository.save(any())).thenReturn(credit);
        when(statementRepository.save(any())).thenReturn(testStatement);

        creditService.completeOfRegistrationAndFullCalculation(finishRequest, statementId);

        verify(statementRepository).findById(statementId);
        verify(clientMapper).toScoringDataDto(finishRequest, testStatement);
        verify(calculatorClientService).calculateCredit(scoringDataDto);
        verify(creditMapper).toCredit(creditDto, CreditStatus.CALCULATED);
        verify(creditRepository).save(any());
        verify(statementRepository).save(testStatement);
    }

    @Test
    @DisplayName("При null запросе выбрасывается NullPointerException")
    void whenFinishRequestIsNullThenThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> creditService.completeOfRegistrationAndFullCalculation(null, statementId));
    }

    @Test
    @DisplayName("При статусе заявки не APPROVED выбрасывается IllegalStateException")
    void whenStatementStatusIsNotApprovedThenThrowIllegalStateException() {
        testStatement.setStatus(ApplicationStatus.PREAPPROVAL);

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));

        assertThrows(IllegalStateException.class,
                () -> creditService.completeOfRegistrationAndFullCalculation(finishRequest, statementId));
    }

    @Test
    @DisplayName("При несуществующем statementId выбрасывается EntityNotFoundException")
    void whenStatementNotFoundThenThrowEntityNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();
        when(statementRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> creditService.completeOfRegistrationAndFullCalculation(finishRequest, nonExistentId));

        assertTrue(exception.getMessage().contains(nonExistentId.toString()));
        verify(creditRepository, never()).save(any());
        verify(statementRepository, never()).save(any());
    }

    @Test
    @DisplayName("При сохранении кредита выбрасывается исключение")
    void whenSavingCreditFailsThenThrowException() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCredit(creditDto, CreditStatus.CALCULATED)).thenReturn(credit);
        when(creditRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class,
                () -> creditService.completeOfRegistrationAndFullCalculation(finishRequest, statementId));

        verify(statementRepository).findById(statementId);
        verify(creditRepository).save(any());
        verify(statementRepository, never()).save(any());
    }

    @Test
    @DisplayName("При успешном расчете статус заявки меняется на CC_APPROVED")
    void whenCreditCalculatedThenStatementStatusChangesToApproved() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCredit(creditDto, CreditStatus.CALCULATED)).thenReturn(credit);
        when(creditRepository.save(any())).thenReturn(credit);
        when(statementRepository.save(any())).thenReturn(testStatement);

        assertEquals(ApplicationStatus.APPROVED, testStatement.getStatus());

        creditService.completeOfRegistrationAndFullCalculation(finishRequest, statementId);

        assertEquals(ApplicationStatus.CC_APPROVED, testStatement.getStatus());
    }

    @Test
    @DisplayName("При успешном расчете в историю статусов добавляется запись")
    void whenCreditCalculatedThenStatusHistoryContainsNewRecord() {
        testStatement.setStatusHistory(new ArrayList<>());

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCredit(creditDto, CreditStatus.CALCULATED)).thenReturn(credit);
        when(creditRepository.save(any())).thenReturn(credit);
        when(statementRepository.save(any())).thenReturn(testStatement);

        assertEquals(0, testStatement.getStatusHistory().size());

        creditService.completeOfRegistrationAndFullCalculation(finishRequest, statementId);

        assertEquals(1, testStatement.getStatusHistory().size());

        StatusHistory history = testStatement.getStatusHistory().getFirst();
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
        when(creditMapper.toCredit(creditDto, CreditStatus.CALCULATED)).thenReturn(credit);
        when(creditRepository.save(any())).thenReturn(credit);
        when(statementRepository.save(any())).thenReturn(testStatement);

        creditService.completeOfRegistrationAndFullCalculation(finishRequest, statementId);

        assertEquals(CreditStatus.CALCULATED, credit.getCreditStatus());
        verify(creditRepository).save(any());
    }

    @Test
    @DisplayName("При успешном расчете кредит содержит все данные из CreditDto")
    void whenCreditCalculatedThenCreditContainsAllDataFromCreditDto() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCredit(creditDto, CreditStatus.CALCULATED)).thenReturn(credit);
        when(creditRepository.save(any())).thenReturn(credit);
        when(statementRepository.save(any())).thenReturn(testStatement);

        creditService.completeOfRegistrationAndFullCalculation(finishRequest, statementId);

        assertThat(credit)
                .usingRecursiveComparison()
                .ignoringFields("creditId", "creditStatus")
                .isEqualTo(creditDto);
    }

    @Test
    @DisplayName("При вызове calculatorClientService.calculateCredit передается корректный ScoringDataDto")
    void whenCreditCalculatedThenScoringDataDtoContainsCorrectData() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCredit(creditDto, CreditStatus.CALCULATED)).thenReturn(credit);
        when(creditRepository.save(any())).thenReturn(credit);
        when(statementRepository.save(any())).thenReturn(testStatement);

        creditService.completeOfRegistrationAndFullCalculation(finishRequest, statementId);

        verify(calculatorClientService).calculateCredit(scoringDataDto);
    }

    @Test
    @DisplayName("При вызове clientMapper.toScoringDataDto передаются корректные параметры")
    void whenCreditCalculatedThenClientMapperReceivesCorrectParameters() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCredit(creditDto, CreditStatus.CALCULATED)).thenReturn(credit);
        when(creditRepository.save(any())).thenReturn(credit);
        when(statementRepository.save(any())).thenReturn(testStatement);

        creditService.completeOfRegistrationAndFullCalculation(finishRequest, statementId);

        verify(clientMapper).toScoringDataDto(finishRequest, testStatement);
    }

    @Test
    @DisplayName("При успешном расчете statement сохраняется в репозитории")
    void whenCreditCalculatedThenStatementIsSaved() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCredit(creditDto, CreditStatus.CALCULATED)).thenReturn(credit);
        when(creditRepository.save(any())).thenReturn(credit);
        when(statementRepository.save(any())).thenReturn(testStatement);

        creditService.completeOfRegistrationAndFullCalculation(finishRequest, statementId);

        verify(statementRepository).save(testStatement);
    }

    @Test
    @DisplayName("При успешном расчете в заявку сохраняется кредит")
    void whenCreditCalculatedThenStatementCreditIsSet() {
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(testStatement));
        when(clientMapper.toScoringDataDto(finishRequest, testStatement)).thenReturn(scoringDataDto);
        when(calculatorClientService.calculateCredit(scoringDataDto)).thenReturn(creditDto);
        when(creditMapper.toCredit(creditDto, CreditStatus.CALCULATED)).thenReturn(credit);
        when(creditRepository.save(any())).thenReturn(credit);
        when(statementRepository.save(any())).thenReturn(testStatement);

        assertNull(testStatement.getCredit());

        creditService.completeOfRegistrationAndFullCalculation(finishRequest, statementId);

        assertNotNull(testStatement.getCredit());
        assertEquals(credit.getCreditId(), testStatement.getCredit().getCreditId());
    }
}