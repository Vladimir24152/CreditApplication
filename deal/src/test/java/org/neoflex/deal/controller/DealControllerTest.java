package org.neoflex.deal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.neoflex.deal.dto.EmploymentDto;
import org.neoflex.deal.dto.FinishRegistrationRequestDto;
import org.neoflex.deal.dto.LoanOfferDto;
import org.neoflex.deal.dto.LoanStatementRequestDto;
import org.neoflex.deal.model.enums.EmploymentStatus;
import org.neoflex.deal.model.enums.Gender;
import org.neoflex.deal.model.enums.MaritalStatus;
import org.neoflex.deal.model.enums.Position;
import org.neoflex.deal.service.CreditService;
import org.neoflex.deal.service.StatementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DealController.class)
@DisplayName("Тесты контроллера DealController")
class DealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StatementService statementService;

    @MockitoBean
    private CreditService creditService;

    private LoanStatementRequestDto loanStatementRequest;
    private List<LoanOfferDto> loanOffers;
    private LoanOfferDto loanOfferRequest;
    private FinishRegistrationRequestDto finishRegistrationRequest;
    private UUID statementId;

    @BeforeEach
    void setUp() {
        statementId = UUID.randomUUID();

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

        loanOffers = Arrays.asList(
                LoanOfferDto.builder()
                        .statementId(statementId)
                        .requestedAmount(BigDecimal.valueOf(1000000))
                        .totalAmount(BigDecimal.valueOf(1005000))
                        .term(12)
                        .monthlyPayment(BigDecimal.valueOf(88848.89))
                        .rate(BigDecimal.valueOf(12.5))
                        .isInsuranceEnabled(true)
                        .isSalaryClient(true)
                        .build(),
                LoanOfferDto.builder()
                        .statementId(statementId)
                        .requestedAmount(BigDecimal.valueOf(1000000))
                        .totalAmount(BigDecimal.valueOf(1005000))
                        .term(12)
                        .monthlyPayment(BigDecimal.valueOf(88848.89))
                        .rate(BigDecimal.valueOf(14.0))
                        .isInsuranceEnabled(false)
                        .isSalaryClient(true)
                        .build()
        );

        loanOfferRequest = LoanOfferDto.builder()
                .statementId(statementId)
                .requestedAmount(BigDecimal.valueOf(1000000))
                .totalAmount(BigDecimal.valueOf(1005000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(87500))
                .rate(BigDecimal.valueOf(12.5))
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .build();

        finishRegistrationRequest = FinishRegistrationRequestDto.builder()
                .gender(Gender.MALE)
                .maritalStatus(MaritalStatus.MARRIED)
                .dependentAmount(2)
                .passportIssueDate(LocalDate.of(2010, 5, 15))
                .passportIssueBranch("123-456")
                .employment(EmploymentDto.builder()
                        .employerInn("1234567890")
                        .employmentStatus(EmploymentStatus.EMPLOYED)
                        .salary(BigDecimal.valueOf(100000))
                        .position(Position.SPECIALIST)
                        .workExperienceTotal(60)
                        .workExperienceCurrent(24)
                        .build())
                .accountNumber("40817810000000000001")
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/deal/statement - успешный расчет кредитных предложений")
    void whenValidLoanStatementRequestThenReturnFourLoanOffers() throws Exception {
        when(statementService.calculationOfPossibleLoanTerms(any(LoanStatementRequestDto.class)))
                .thenReturn(loanOffers);

        ResultActions result = mockMvc.perform(post("/api/v1/deal/statement")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanStatementRequest)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].statementId", is(statementId.toString())))
                .andExpect(jsonPath("$[0].requestedAmount", is(1000000)))
                .andExpect(jsonPath("$[0].term", is(12)))
                .andExpect(jsonPath("$[0].rate", is(12.5)))
                .andExpect(jsonPath("$[1].rate", is(14.0)));

        verify(statementService).calculationOfPossibleLoanTerms(any(LoanStatementRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/v1/deal/statement - невалидные данные возвращают 400")
    void whenInvalidLoanStatementRequestThenReturnBadRequest() throws Exception {
        LoanStatementRequestDto invalidRequest = LoanStatementRequestDto.builder()
                .amount(BigDecimal.valueOf(10000))
                .term(3)
                .firstName("I")
                .lastName("Ivanov")
                .email("invalid-email")
                .birthDate(LocalDate.now())
                .passportSeries("123")
                .passportNumber("56789")
                .build();

        ResultActions result = mockMvc.perform(post("/api/v1/deal/statement")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        result.andExpect(status().isBadRequest());

        verifyNoInteractions(statementService);
    }

    @Test
    @DisplayName("POST /api/v1/deal/offer/select - успешный выбор кредитного предложения")
    void whenValidLoanOfferRequestThenSelectOfferSuccessfully() throws Exception {
        doNothing().when(statementService).choosingOneOfTheLoanOffers(any(LoanOfferDto.class));

        ResultActions result = mockMvc.perform(post("/api/v1/deal/offer/select")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanOfferRequest)));

        result.andExpect(status().isOk());

        verify(statementService).choosingOneOfTheLoanOffers(any(LoanOfferDto.class));
    }

    @Test
    @DisplayName("POST /api/v1/deal/offer/select - невалидный LoanOfferDto возвращает 400")
    void whenInvalidLoanOfferRequestThenReturnBadRequest() throws Exception {
        LoanOfferDto invalidRequest = LoanOfferDto.builder()
                .statementId(null)
                .requestedAmount(BigDecimal.valueOf(-1000))
                .term(0)
                .build();

        ResultActions result = mockMvc.perform(post("/api/v1/deal/offer/select")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        result.andExpect(status().isBadRequest());

        verifyNoInteractions(statementService);
    }

    @Test
    @DisplayName("POST /api/v1/deal/calculate/{statementId} - успешное завершение регистрации")
    void whenValidFinishRegistrationRequestThenCalculateCreditSuccessfully() throws Exception {
        doNothing().when(creditService).completeOfRegistrationAndFullCalculation(
                any(FinishRegistrationRequestDto.class), any(UUID.class));

        ResultActions result = mockMvc.perform(post("/api/v1/deal/calculate/{statementId}", statementId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(finishRegistrationRequest)));

        result.andExpect(status().isOk());

        verify(creditService).completeOfRegistrationAndFullCalculation(
                any(FinishRegistrationRequestDto.class), eq(statementId));
    }

    @Test
    @DisplayName("POST /api/v1/deal/calculate/{statementId} - невалидный запрос возвращает 400")
    void whenInvalidFinishRegistrationRequestThenReturnBadRequest() throws Exception {
        FinishRegistrationRequestDto invalidRequest = FinishRegistrationRequestDto.builder()
                .gender(null)
                .maritalStatus(null)
                .dependentAmount(-1)
                .passportIssueDate(LocalDate.now().plusDays(1))
                .passportIssueBranch("12345")
                .employment(null)
                .accountNumber("123")
                .build();

        ResultActions result = mockMvc.perform(post("/api/v1/deal/calculate/{statementId}", statementId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        result.andExpect(status().isBadRequest());

        verifyNoInteractions(creditService);
    }

    @Test
    @DisplayName("POST /api/v1/deal/calculate/{statementId} - ошибка сервиса возвращает 404")
    void whenStatementNotFoundThenReturnNotFound() throws Exception {
        doThrow(new jakarta.persistence.EntityNotFoundException("Заявка не найдена"))
                .when(creditService).completeOfRegistrationAndFullCalculation(
                        any(FinishRegistrationRequestDto.class), any(UUID.class));

        ResultActions result = mockMvc.perform(post("/api/v1/deal/calculate/{statementId}", statementId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(finishRegistrationRequest)));

        result.andExpect(status().isNotFound());

        verify(creditService).completeOfRegistrationAndFullCalculation(any(), any());
    }

    @Test
    @DisplayName("POST /api/v1/deal/calculate/{statementId} - ошибка валидации в сервисе возвращает 400")
    void whenScoringFailedThenReturnBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Неверные данные для скоринга"))
                .when(creditService).completeOfRegistrationAndFullCalculation(
                        any(FinishRegistrationRequestDto.class), any(UUID.class));

        ResultActions result = mockMvc.perform(post("/api/v1/deal/calculate/{statementId}", statementId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(finishRegistrationRequest)));

        result.andExpect(status().isBadRequest());

        verify(creditService).completeOfRegistrationAndFullCalculation(any(), any());
    }
}