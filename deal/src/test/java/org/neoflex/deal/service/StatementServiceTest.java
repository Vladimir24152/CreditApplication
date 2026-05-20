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
import org.neoflex.deal.dto.LoanOfferDto;
import org.neoflex.deal.dto.LoanStatementRequestDto;
import org.neoflex.deal.dto.StatementResponseDto;
import org.neoflex.deal.mapper.ClientMapper;
import org.neoflex.deal.mapper.StatementMapper;
import org.neoflex.deal.model.Client;
import org.neoflex.deal.model.Statement;
import org.neoflex.deal.model.enums.ApplicationStatus;
import org.neoflex.deal.model.enums.ChangeType;
import org.neoflex.deal.model.jsonb.StatusHistory;
import org.neoflex.deal.producer.KafkaProducerService;
import org.neoflex.deal.repository.ClientRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Тесты сервиса StatementService")
@ExtendWith(MockitoExtension.class)
class StatementServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private CalculatorClientService calculatorClientService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private StatementMapper statementMapper;

    @InjectMocks
    private StatementService statementService;

    private LoanStatementRequestDto loanStatementRequest;
    private LoanOfferDto loanOfferRequest;
    private Client testClient;
    private Statement testStatement;
    private List<LoanOfferDto> mockOffers;

    @BeforeEach
    void setUp() {
        loanStatementRequest = LoanStatementRequestDto.builder()
                .amount(BigDecimal.valueOf(1000000))
                .term(12)
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .email("ivan@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .passportSeries("1234")
                .passportNumber("567890")
                .build();

        testClient = Client.builder()
                .clientId(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .email("ivan@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        testStatement = Statement.builder()
                .statementId(UUID.randomUUID())
                .client(testClient)
                .status(ApplicationStatus.PREAPPROVAL)
                .statusHistory(new ArrayList<>())
                .creationDate(LocalDateTime.now())
                .build();

        mockOffers = List.of(
                createLoanOffer(testStatement.getStatementId(), true, true),
                createLoanOffer(testStatement.getStatementId(), false, true),
                createLoanOffer(testStatement.getStatementId(), true, false),
                createLoanOffer(testStatement.getStatementId(), false, false)
        );

        loanOfferRequest = LoanOfferDto.builder()
                .statementId(testStatement.getStatementId())
                .requestedAmount(BigDecimal.valueOf(1000000))
                .totalAmount(BigDecimal.valueOf(1005000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(87500))
                .rate(BigDecimal.valueOf(12.5))
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .build();
    }

    private LoanOfferDto createLoanOffer(UUID statementId, Boolean insurance, Boolean salary) {
        return LoanOfferDto.builder()
                .statementId(statementId)
                .requestedAmount(BigDecimal.valueOf(1000000))
                .totalAmount(BigDecimal.valueOf(1005000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(87500))
                .rate(BigDecimal.valueOf(insurance && salary ? 11.0 : 15.0))
                .isInsuranceEnabled(insurance)
                .isSalaryClient(salary)
                .build();
    }

    @Test
    @DisplayName("Успешный расчет возможных условий кредита при корректных данных клиента")
    void whenClientDataIsValidThenReturnFourLoanOffersWithStatementId() {
        when(clientMapper.toClient(loanStatementRequest)).thenReturn(testClient);
        when(clientRepository.save(any())).thenReturn(testClient);
        when(statementRepository.save(any())).thenReturn(testStatement);
        when(calculatorClientService.calculateLoanOffers(loanStatementRequest)).thenReturn(mockOffers);

        List<LoanOfferDto> result = statementService.calculateTerms(loanStatementRequest);

        assertNotNull(result);
        assertEquals(4, result.size());

        result.forEach(offer ->
                assertEquals(testStatement.getStatementId(), offer.getStatementId())
        );

        verify(clientRepository).save(any());
        verify(statementRepository).save(any());
        verify(calculatorClientService).calculateLoanOffers(loanStatementRequest);
    }

    @Test
    @DisplayName("При null запросе выбрасывается NullPointerException")
    void whenRequestIsNullThenThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> statementService.calculateTerms(null));
    }

    @Test
    @DisplayName("При сохранении клиента выбрасывается исключение")
    void whenSavingClientFailsThenThrowException() {
        when(clientMapper.toClient(loanStatementRequest)).thenReturn(testClient);
        when(clientRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class,
                () -> statementService.calculateTerms(loanStatementRequest));

        verifyNoInteractions(statementRepository);
    }

    @Test
    @DisplayName("Успешный выбор кредитного предложения")
    void whenLoanOfferSelectedThenStatementUpdated() {
        when(statementRepository.findById(loanOfferRequest.getStatementId()))
                .thenReturn(Optional.of(testStatement));
        when(statementRepository.save(any())).thenReturn(testStatement);
        doNothing().when(kafkaProducerService).send(any());

        statementService.selectOffer(loanOfferRequest);

        assertEquals(ApplicationStatus.APPROVED, testStatement.getStatus());
        assertEquals(loanOfferRequest, testStatement.getAppliedOffer());

        assertNotNull(testStatement.getStatusHistory());
        assertEquals(1, testStatement.getStatusHistory().size());

        StatusHistory history = testStatement.getStatusHistory().getFirst();
        assertEquals(ApplicationStatus.APPROVED, history.getStatus());
        assertEquals(ChangeType.AUTOMATIC, history.getChangeType());
        assertNotNull(history.getTime());

        verify(statementRepository).save(testStatement);
    }

    @Test
    @DisplayName("При выборе предложения с несуществующим statementId выбрасывается EntityNotFoundException")
    void whenStatementNotFoundThenThrowEntityNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();
        loanOfferRequest.setStatementId(nonExistentId);
        when(statementRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> statementService.selectOffer(loanOfferRequest));

        assertTrue(exception.getMessage().contains(nonExistentId.toString()));
        verify(statementRepository, never()).save(any());
    }

    @Test
    @DisplayName("При null запросе на выбор предложения выбрасывается NullPointerException")
    void whenLoanOfferRequestIsNullThenThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> statementService.selectOffer(null));
    }

    @Test
    @DisplayName("Проверка что история статусов сохраняется корректно")
    void whenStatusChangesThenHistoryPreservesOrder() {
        testStatement.setStatusHistory(new ArrayList<>());
        testStatement.setStatus(ApplicationStatus.PREAPPROVAL);
        doNothing().when(kafkaProducerService).send(any());

        when(statementRepository.findById(loanOfferRequest.getStatementId()))
                .thenReturn(Optional.of(testStatement));
        when(statementRepository.save(any())).thenReturn(testStatement);

        statementService.selectOffer(loanOfferRequest);

        assertEquals(1, testStatement.getStatusHistory().size());
        assertEquals(ApplicationStatus.APPROVED, testStatement.getStatusHistory().getFirst().getStatus());
    }

    @Test
    @DisplayName("Успешное создание клиента из LoanStatementRequestDto")
    void whenClientMapperCalledThenClientIsCreated() {
        when(clientMapper.toClient(loanStatementRequest)).thenReturn(testClient);
        when(clientRepository.save(any())).thenReturn(testClient);
        when(statementRepository.save(any())).thenReturn(testStatement);
        when(calculatorClientService.calculateLoanOffers(loanStatementRequest)).thenReturn(mockOffers);

        List<LoanOfferDto> result = statementService.calculateTerms(loanStatementRequest);

        assertNotNull(result);
        verify(clientMapper).toClient(loanStatementRequest);
        verify(clientRepository).save(any());
        verify(statementRepository).save(any());
    }

    @Test
    @DisplayName("Проверка что все 4 кредитных предложения получают одинаковый statementId")
    void whenLoanOffersGeneratedThenAllHaveSameStatementId() {
        when(clientMapper.toClient(loanStatementRequest)).thenReturn(testClient);
        when(clientRepository.save(any())).thenReturn(testClient);
        when(statementRepository.save(any())).thenReturn(testStatement);
        when(calculatorClientService.calculateLoanOffers(loanStatementRequest)).thenReturn(mockOffers);

        List<LoanOfferDto> result = statementService.calculateTerms(loanStatementRequest);

        UUID firstStatementId = result.getFirst().getStatementId();
        for (LoanOfferDto offer : result) {
            assertEquals(firstStatementId, offer.getStatementId());
        }

        verify(clientMapper).toClient(loanStatementRequest);
        verify(clientRepository).save(any());
        verify(statementRepository).save(any());
        verify(calculatorClientService).calculateLoanOffers(loanStatementRequest);
    }

    @Test
    @DisplayName("При выборе предложения статус заявки меняется с PREAPPROVAL на APPROVED")
    void whenLoanOfferSelectedThenStatusChangesFromPreapprovalToApproved() {
        testStatement.setStatus(ApplicationStatus.PREAPPROVAL);
        when(statementRepository.findById(loanOfferRequest.getStatementId()))
                .thenReturn(Optional.of(testStatement));
        when(statementRepository.save(any())).thenReturn(testStatement);
        doNothing().when(kafkaProducerService).send(any());

        statementService.selectOffer(loanOfferRequest);

        assertEquals(ApplicationStatus.APPROVED, testStatement.getStatus());
        assertNotEquals(ApplicationStatus.PREAPPROVAL, testStatement.getStatus());
    }

    @Test
    @DisplayName("При выборе предложения appliedOffer сохраняется корректно")
    void whenLoanOfferSelectedThenAppliedOfferIsSet() {
        when(statementRepository.findById(loanOfferRequest.getStatementId()))
                .thenReturn(Optional.of(testStatement));
        when(statementRepository.save(any())).thenReturn(testStatement);
        doNothing().when(kafkaProducerService).send(any());

        statementService.selectOffer(loanOfferRequest);

        assertThat(testStatement.getAppliedOffer())
                .usingRecursiveComparison()
                .isEqualTo(loanOfferRequest);
    }

    @Test
    @DisplayName("При выборе предложения время изменения статуса не должно быть null")
    void whenLoanOfferSelectedThenStatusChangeTimeIsNotNull() {
        when(statementRepository.findById(loanOfferRequest.getStatementId()))
                .thenReturn(Optional.of(testStatement));
        when(statementRepository.save(any())).thenReturn(testStatement);
        doNothing().when(kafkaProducerService).send(any());

        statementService.selectOffer(loanOfferRequest);

        StatusHistory history = testStatement.getStatusHistory().getFirst();
        assertNotNull(history.getTime());
    }

    @Test
    @DisplayName("Проверка что при создании заявки поле creationDate заполняется")
    void whenStatementCreatedThenCreationDateIsSet() {
        when(clientMapper.toClient(loanStatementRequest)).thenReturn(testClient);
        when(clientRepository.save(any())).thenReturn(testClient);
        when(statementRepository.save(any())).thenReturn(testStatement);
        when(calculatorClientService.calculateLoanOffers(loanStatementRequest)).thenReturn(mockOffers);

        List<LoanOfferDto> result = statementService.calculateTerms(loanStatementRequest);

        assertNotNull(result);
        assertNotNull(testStatement.getCreationDate());

        verify(clientMapper).toClient(loanStatementRequest);
        verify(clientRepository).save(any());
        verify(statementRepository).save(any());
        verify(calculatorClientService).calculateLoanOffers(loanStatementRequest);
    }

    @Test
    @DisplayName("При получении несуществующей заявки выбрасывается EntityNotFoundException")
    void whenGetStatementStatementByIdNotFoundThenThrowEntityNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();

        when(statementRepository.findFullStatementById(nonExistentId))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> statementService.getStatement(nonExistentId));

        assertTrue(exception.getMessage().contains(nonExistentId.toString()));
        verify(statementRepository).findFullStatementById(nonExistentId);
    }

    @Test
    @DisplayName("Получение всех заявок при пустом списке возвращает пустой список")
    void whenGetStatementAllStatementsReturnsEmptyList() {
        when(statementRepository.findAllFullStatementById()).thenReturn(new ArrayList<>());

        List<StatementResponseDto> result = statementService.getAllStatements();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(statementRepository).findAllFullStatementById();
    }
}