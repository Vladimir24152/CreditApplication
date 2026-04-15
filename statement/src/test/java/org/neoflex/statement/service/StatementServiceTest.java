package org.neoflex.statement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neoflex.statement.client.deal.DealClientService;
import org.neoflex.statement.dto.LoanOfferDto;
import org.neoflex.statement.dto.LoanStatementRequestDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Тесты сервиса StatementService")
@ExtendWith(MockitoExtension.class)
class StatementServiceTest {

    @Mock
    private DealClientService dealClientService;

    @InjectMocks
    private StatementService statementService;

    private LoanStatementRequestDto loanStatementRequest;
    private LoanOfferDto loanOfferRequest;
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

        mockOffers = List.of(
                createLoanOffer(UUID.randomUUID(), true, true),
                createLoanOffer(UUID.randomUUID(), false, true),
                createLoanOffer(UUID.randomUUID(), true, false),
                createLoanOffer(UUID.randomUUID(), false, false)
        );

        loanOfferRequest = LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
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
    void whenClientDataIsValidThenReturnFourLoanOffers() {
        when(dealClientService.calculateOfPossibleLoanTerms(loanStatementRequest)).thenReturn(mockOffers);

        List<LoanOfferDto> result = statementService.getOffers(loanStatementRequest);

        assertNotNull(result);
        assertEquals(4, result.size());

        verify(dealClientService).calculateOfPossibleLoanTerms(loanStatementRequest);
    }

    @Test
    @DisplayName("При null запросе выбрасывается NullPointerException")
    void whenRequestIsNullThenThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> statementService.getOffers(null));
    }

    @Test
    @DisplayName("Успешный выбор предложения при корректных данных")
    void whenOfferIsValidThenSelectOfferSuccess() {
        doNothing().when(dealClientService).selectOffer(loanOfferRequest);

        statementService.selectOffer(loanOfferRequest);

        verify(dealClientService).selectOffer(loanOfferRequest);
    }

    @Test
    @DisplayName("При null запросе в selectOffer выбрасывается NullPointerException")
    void whenSelectOfferRequestIsNullThenThrowNullPointerException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> statementService.selectOffer(null));
    }
}