package org.neoflex.deal.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.neoflex.deal.dto.DealDocumentDto;
import org.neoflex.deal.dto.LoanOfferDto;
import org.neoflex.deal.dto.PaymentScheduleElementDto;
import org.neoflex.deal.dto.StatementResponseDto;
import org.neoflex.deal.model.Client;
import org.neoflex.deal.model.Credit;
import org.neoflex.deal.model.Statement;
import org.neoflex.deal.model.enums.ApplicationStatus;
import org.neoflex.deal.model.enums.CreditStatus;
import org.neoflex.deal.model.enums.Gender;
import org.neoflex.deal.model.enums.MaritalStatus;
import org.neoflex.deal.model.jsonb.Passport;
import org.neoflex.deal.model.jsonb.StatusHistory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Тесты маппера StatementMapper")
class StatementMapperTest {

    private StatementMapper statementMapper;

    private Statement statement;
    private LocalDate signDate;

    @BeforeEach
    void setUp() {
        statementMapper = Mappers.getMapper(StatementMapper.class);
        signDate = LocalDate.now();

        Passport passport = Passport.builder()
                .series("1234")
                .number("567890")
                .issueDate(LocalDate.of(2010, 5, 15))
                .issueBranch("123-456")
                .build();

        Client client = Client.builder()
                .clientId(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Petrov")
                .middleName("Ivanovich")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("ivan@example.com")
                .gender(Gender.MALE)
                .maritalStatus(MaritalStatus.MARRIED)
                .dependentAmount(2)
                .accountNumber("40817810000000000001")
                .passport(passport)
                .build();

        List<PaymentScheduleElementDto> paymentSchedule = List.of(
                PaymentScheduleElementDto.builder()
                        .number(1)
                        .date(LocalDate.now().plusMonths(1))
                        .totalPayment(BigDecimal.valueOf(50000))
                        .principalPayment(BigDecimal.valueOf(40000))
                        .interestPayment(BigDecimal.valueOf(10000))
                        .remainingDebt(BigDecimal.valueOf(960000))
                        .build()
        );

        Credit credit = Credit.builder()
                .creditId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(1000000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(50000))
                .rate(BigDecimal.valueOf(12))
                .psk(BigDecimal.valueOf(600000))
                .isInsuranceEnabled(true)
                .isSalaryClient(true)
                .paymentSchedule(paymentSchedule)
                .creditStatus(CreditStatus.CALCULATED)
                .build();

        LoanOfferDto appliedOffer = LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(BigDecimal.valueOf(1000000))
                .totalAmount(BigDecimal.valueOf(1100000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(50000))
                .rate(BigDecimal.valueOf(12))
                .isInsuranceEnabled(true)
                .isSalaryClient(true)
                .build();

        statement = Statement.builder()
                .statementId(UUID.randomUUID())
                .client(client)
                .credit(credit)
                .status(ApplicationStatus.APPROVED)
                .creationDate(LocalDateTime.now())
                .signDate(LocalDateTime.now())
                .appliedOffer(appliedOffer)
                .sesCode("123456")
                .statusHistory(List.of(StatusHistory.builder()
                        .status(ApplicationStatus.PREAPPROVAL)
                        .time(LocalDateTime.now())
                        .build()))
                .build();
    }

    @Test
    @DisplayName("Маппинг Statement и даты подписания в DealDocumentDto должен создавать DTO со всеми полями")
    void toDealDocumentDtoShouldMapAllFieldsCorrectly() {
        DealDocumentDto result = statementMapper.toDealDocumentDto(statement, signDate);

        assertNotNull(result);

        assertEquals(statement.getStatementId(), result.getStatementId());
        assertEquals(signDate, result.getSignDate());

        assertEquals(statement.getClient().getFirstName(), result.getFirstName());
        assertEquals(statement.getClient().getLastName(), result.getLastName());
        assertEquals(statement.getClient().getMiddleName(), result.getMiddleName());
        assertEquals(statement.getClient().getBirthDate(), result.getBirthDate());
        assertEquals(statement.getClient().getEmail(), result.getEmail());
        assertEquals(statement.getClient().getGender(), result.getGender());
        assertEquals(statement.getClient().getMaritalStatus(), result.getMaritalStatus());
        assertEquals(statement.getClient().getDependentAmount(), result.getDependentAmount());
        assertEquals(statement.getClient().getAccountNumber(), result.getAccountNumber());

        assertEquals(statement.getClient().getPassport().getSeries(), result.getPassportSeries());
        assertEquals(statement.getClient().getPassport().getNumber(), result.getPassportNumber());
        assertEquals(statement.getClient().getPassport().getIssueDate(), result.getPassportIssueDate());
        assertEquals(statement.getClient().getPassport().getIssueBranch(), result.getPassportIssueBranch());

        assertEquals(statement.getCredit().getAmount(), result.getAmount());
        assertEquals(statement.getCredit().getTerm(), result.getTerm());
        assertEquals(statement.getCredit().getMonthlyPayment(), result.getMonthlyPayment());
        assertEquals(statement.getCredit().getRate(), result.getRate());
        assertEquals(statement.getCredit().getPsk(), result.getPsk());

        assertEquals(statement.getAppliedOffer().getIsInsuranceEnabled(), result.getIsInsuranceEnabled());
        assertEquals(statement.getAppliedOffer().getIsSalaryClient(), result.getIsSalaryClient());

        assertThat(result.getPaymentSchedule())
                .usingRecursiveComparison()
                .isEqualTo(statement.getCredit().getPaymentSchedule());
    }

    @Test
    @DisplayName("Маппинг Statement должен устанавливать переданную дату подписания")
    void toDealDocumentDtoShouldSetProvidedSignDate() {
        LocalDate expectedSignDate = LocalDate.of(2025, 12, 31);
        DealDocumentDto result = statementMapper.toDealDocumentDto(statement, expectedSignDate);

        assertEquals(expectedSignDate, result.getSignDate());
    }

    @Test
    @DisplayName("Маппинг Statement с null полями должен корректно обрабатывать null значения")
    void toDealDocumentDtoShouldHandleNullFields() {
        Statement nullStatement = Statement.builder()
                .statementId(UUID.randomUUID())
                .build();

        DealDocumentDto result = statementMapper.toDealDocumentDto(nullStatement, signDate);

        assertNotNull(result);
        assertEquals(nullStatement.getStatementId(), result.getStatementId());
        assertEquals(signDate, result.getSignDate());
        assertThat(result.getFirstName()).isNull();
        assertThat(result.getLastName()).isNull();
        assertThat(result.getPassportSeries()).isNull();
        assertThat(result.getAmount()).isNull();
    }

    @Test
    @DisplayName("Маппинг Statement в StatementResponseDto должен корректно преобразовывать все поля")
    void toStatementResponseDtoShouldMapAllFieldsCorrectly() {
        StatementResponseDto result = statementMapper.toStatementResponseDto(statement);

        assertNotNull(result);
        assertEquals(statement.getStatementId(), result.getStatementId());
        assertEquals(statement.getStatus(), result.getStatus());
        assertEquals(statement.getCreationDate(), result.getCreationDate());
        assertEquals(statement.getAppliedOffer(), result.getAppliedOffer());
        assertEquals(statement.getSignDate(), result.getSignDate());
        assertEquals(statement.getStatusHistory(), result.getStatusHistory());
    }

    @Test
    @DisplayName("Маппинг Statement в StatementResponseDto должен корректно маппить клиента")
    void toStatementResponseDtoShouldMapClientCorrectly() {
        StatementResponseDto result = statementMapper.toStatementResponseDto(statement);

        assertNotNull(result.getClient());
        assertEquals(statement.getClient().getFirstName(), result.getClient().getFirstName());
        assertEquals(statement.getClient().getLastName(), result.getClient().getLastName());
        assertEquals(statement.getClient().getMiddleName(), result.getClient().getMiddleName());
        assertEquals(statement.getClient().getBirthDate(), result.getClient().getBirthDate());
        assertEquals(statement.getClient().getEmail(), result.getClient().getEmail());
        assertEquals(statement.getClient().getGender(), result.getClient().getGender());
        assertEquals(statement.getClient().getMaritalStatus(), result.getClient().getMaritalStatus());
        assertEquals(statement.getClient().getDependentAmount(), result.getClient().getDependentAmount());
        assertEquals(statement.getClient().getAccountNumber(), result.getClient().getAccountNumber());

        assertThat(result.getClient().getPassport())
                .usingRecursiveComparison()
                .isEqualTo(statement.getClient().getPassport());
    }

    @Test
    @DisplayName("Маппинг Statement в StatementResponseDto должен корректно маппить кредит")
    void toStatementResponseDtoShouldMapCreditCorrectly() {
        StatementResponseDto result = statementMapper.toStatementResponseDto(statement);

        assertNotNull(result.getCredit());
        assertEquals(statement.getCredit().getAmount(), result.getCredit().getAmount());
        assertEquals(statement.getCredit().getTerm(), result.getCredit().getTerm());
        assertEquals(statement.getCredit().getMonthlyPayment(), result.getCredit().getMonthlyPayment());
        assertEquals(statement.getCredit().getRate(), result.getCredit().getRate());
        assertEquals(statement.getCredit().getPsk(), result.getCredit().getPsk());
        assertEquals(statement.getCredit().getIsInsuranceEnabled(), result.getCredit().getIsInsuranceEnabled());
        assertEquals(statement.getCredit().getIsSalaryClient(), result.getCredit().getIsSalaryClient());

        assertThat(result.getCredit().getPaymentSchedule())
                .usingRecursiveComparison()
                .isEqualTo(statement.getCredit().getPaymentSchedule());
    }

    @Test
    @DisplayName("Маппинг Statement с null кредитом должен корректно обрабатывать null")
    void toStatementResponseDtoShouldHandleNullCredit() {
        Statement statementWithoutCredit = Statement.builder()
                .statementId(statement.getStatementId())
                .client(statement.getClient())
                .status(ApplicationStatus.PREAPPROVAL)
                .creationDate(LocalDateTime.now())
                .statusHistory(statement.getStatusHistory())
                .build();

        StatementResponseDto result = statementMapper.toStatementResponseDto(statementWithoutCredit);

        assertNotNull(result);
        assertNotNull(result.getClient());
        assertThat(result.getCredit()).isNull();
    }

    @Test
    @DisplayName("Маппинг Statement с null полями должен корректно обрабатывать null значения в StatementResponseDto")
    void toStatementResponseDtoShouldHandleNullFields() {
        Statement nullStatement = Statement.builder()
                .statementId(UUID.randomUUID())
                .build();

        StatementResponseDto result = statementMapper.toStatementResponseDto(nullStatement);

        assertNotNull(result);
        assertEquals(nullStatement.getStatementId(), result.getStatementId());
        assertThat(result.getClient()).isNull();
        assertThat(result.getCredit()).isNull();
        assertThat(result.getStatus()).isNull();
        assertThat(result.getCreationDate()).isNull();
        assertThat(result.getAppliedOffer()).isNull();
        assertThat(result.getSignDate()).isNull();
        assertThat(result.getStatusHistory()).isNull();
    }
}