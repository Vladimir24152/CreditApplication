package org.neoflex.deal.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.neoflex.deal.dto.EmploymentDto;
import org.neoflex.deal.dto.FinishRegistrationRequestDto;
import org.neoflex.deal.dto.LoanOfferDto;
import org.neoflex.deal.dto.LoanStatementRequestDto;
import org.neoflex.deal.dto.ScoringDataDto;
import org.neoflex.deal.model.Client;
import org.neoflex.deal.model.Statement;
import org.neoflex.deal.model.enums.Gender;
import org.neoflex.deal.model.enums.MaritalStatus;
import org.neoflex.deal.model.enums.Position;
import org.neoflex.deal.model.jsonb.Passport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Тесты маппера ClientMapper")
class ClientMapperTest {

    private ClientMapper clientMapper;

    private LoanStatementRequestDto loanStatementRequest;
    private FinishRegistrationRequestDto finishRegistrationRequest;
    private Statement statement;
    private Client client;
    private LoanOfferDto appliedOffer;

    @BeforeEach
    void setUp() {

        clientMapper = Mappers.getMapper(ClientMapper.class);

        loanStatementRequest = LoanStatementRequestDto.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .birthDate(LocalDate.of(1990, 1, 15))
                .email("ivan@example.com")
                .passportSeries("1234")
                .passportNumber("567890")
                .build();

        finishRegistrationRequest = FinishRegistrationRequestDto.builder()
                .gender(Gender.MALE)
                .maritalStatus(MaritalStatus.MARRIED)
                .dependentAmount(2)
                .passportIssueDate(LocalDate.of(2010, 5, 15))
                .passportIssueBranch("123-456")
                .employment(EmploymentDto.builder()
                        .employerInn("1234567890")
                        .salary(BigDecimal.valueOf(100000))
                        .position(Position.SPECIALIST)
                        .workExperienceTotal(60)
                        .workExperienceCurrent(24)
                        .build())
                .accountNumber("40817810000000000001")
                .build();

        client = Client.builder()
                .clientId(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .birthDate(LocalDate.of(1990, 1, 15))
                .email("ivan@example.com")
                .passport(Passport.builder()
                        .series("1234")
                        .number("567890")
                        .build())
                .build();

        appliedOffer = LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(BigDecimal.valueOf(1000000))
                .term(12)
                .isInsuranceEnabled(true)
                .isSalaryClient(true)
                .rate(BigDecimal.valueOf(15.0))
                .totalAmount(BigDecimal.valueOf(1066186.68))
                .monthlyPayment(BigDecimal.valueOf(88848.89))
                .build();

        statement = Statement.builder()
                .statementId(UUID.randomUUID())
                .client(client)
                .appliedOffer(appliedOffer)
                .build();
    }

    @Test
    @DisplayName("Маппинг LoanStatementRequestDto в Client должен создавать клиента с корректными полями")
    void toClientShouldMapAllFieldsCorrectly() {
        Client result = clientMapper.toClient(loanStatementRequest);

        assertNotNull(result);

        assertNull(result.getClientId());
        assertNull(result.getGender());
        assertNull(result.getMaritalStatus());
        assertNull(result.getDependentAmount());
        assertNull(result.getEmployment());
        assertNull(result.getAccountNumber());

        assertEquals(loanStatementRequest.getFirstName(), result.getFirstName());
        assertEquals(loanStatementRequest.getLastName(), result.getLastName());
        assertEquals(loanStatementRequest.getMiddleName(), result.getMiddleName());
        assertEquals(loanStatementRequest.getBirthDate(), result.getBirthDate());
        assertEquals(loanStatementRequest.getEmail(), result.getEmail());

        assertNotNull(result.getPassport());
        assertEquals(loanStatementRequest.getPassportSeries(), result.getPassport().getSeries());
        assertEquals(loanStatementRequest.getPassportNumber(), result.getPassport().getNumber());
    }

    @Test
    @DisplayName("Маппинг LoanStatementRequestDto в Passport должен создавать паспорт с серией и номером")
    void toPassportShouldMapSeriesAndNumberCorrectly() {
        Passport result = clientMapper.toPassport(loanStatementRequest);

        assertNotNull(result);

        assertEquals(loanStatementRequest.getPassportSeries(), result.getSeries());
        assertEquals(loanStatementRequest.getPassportNumber(), result.getNumber());
        assertNull(result.getIssueBranch());
        assertNull(result.getIssueDate());
    }

    @Test
    @DisplayName("Маппинг FinishRegistrationRequestDto и Statement в ScoringDataDto должен объединять данные из обоих источников")
    void toScoringDataDtoShouldCombineDataFromRequestAndStatement() {
        ScoringDataDto result = clientMapper.toScoringDataDto(finishRegistrationRequest, statement);

        assertNotNull(result);

        assertEquals(appliedOffer.getRequestedAmount(), result.getAmount());
        assertEquals(appliedOffer.getTerm(), result.getTerm());
        assertEquals(client.getFirstName(), result.getFirstName());
        assertEquals(client.getLastName(), result.getLastName());
        assertEquals(client.getMiddleName(), result.getMiddleName());
        assertEquals(client.getBirthDate(), result.getBirthDate());
        assertEquals(finishRegistrationRequest.getGender(), result.getGender());
        assertEquals(client.getPassport().getSeries(), result.getPassportSeries());
        assertEquals(client.getPassport().getNumber(), result.getPassportNumber());
        assertEquals(finishRegistrationRequest.getPassportIssueDate(), result.getPassportIssueDate());
        assertEquals(finishRegistrationRequest.getPassportIssueBranch(), result.getPassportIssueBranch());
        assertEquals(finishRegistrationRequest.getMaritalStatus(), result.getMaritalStatus());
        assertEquals(finishRegistrationRequest.getDependentAmount(), result.getDependentAmount());
        assertEquals(finishRegistrationRequest.getEmployment(), result.getEmployment());
        assertEquals(finishRegistrationRequest.getAccountNumber(), result.getAccountNumber());
        assertEquals(appliedOffer.getIsInsuranceEnabled(), result.getIsInsuranceEnabled());
        assertEquals(appliedOffer.getIsSalaryClient(), result.getIsSalaryClient());
    }

    @Test
    @DisplayName("Маппинг LoanStatementRequestDto в Passport должен игнорировать issueBranch и issueDate")
    void toPassportShouldIgnoreIssueBranchAndIssueDate() {
        Passport result = clientMapper.toPassport(loanStatementRequest);

        assertNull(result.getIssueBranch());
        assertNull(result.getIssueDate());
    }

    @Test
    @DisplayName("Маппинг в ScoringDataDto должен корректно маппить Employment из запроса")
    void toScoringDataDtoShouldMapEmploymentCorrectly() {
        ScoringDataDto result = clientMapper.toScoringDataDto(finishRegistrationRequest, statement);

        assertNotNull(result.getEmployment());
        assertEquals(finishRegistrationRequest.getEmployment().getEmployerInn(),
                result.getEmployment().getEmployerInn());
        assertEquals(finishRegistrationRequest.getEmployment().getSalary(), result.getEmployment().getSalary());
        assertEquals(finishRegistrationRequest.getEmployment().getPosition(), result.getEmployment().getPosition());
        assertEquals(finishRegistrationRequest.getEmployment().getWorkExperienceTotal(),
                result.getEmployment().getWorkExperienceTotal());
        assertEquals(finishRegistrationRequest.getEmployment().getWorkExperienceCurrent(),
                result.getEmployment().getWorkExperienceCurrent());
    }
}