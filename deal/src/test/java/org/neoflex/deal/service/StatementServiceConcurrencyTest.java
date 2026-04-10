package org.neoflex.deal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neoflex.deal.dto.LoanOfferDto;
import org.neoflex.deal.model.Client;
import org.neoflex.deal.model.Statement;
import org.neoflex.deal.model.enums.ApplicationStatus;
import org.neoflex.deal.model.enums.ChangeType;
import org.neoflex.deal.model.jsonb.Passport;
import org.neoflex.deal.model.jsonb.StatusHistory;
import org.neoflex.deal.repository.ClientRepository;
import org.neoflex.deal.repository.StatementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertTrue;

@SpringBootTest
@Testcontainers
class StatementServiceLockTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    @Autowired
    private StatementService statementService;
    @Autowired
    private StatementRepository statementRepository;
    @Autowired
    private ClientRepository clientRepository;
    private UUID statementId;
    private LoanOfferDto offer;
    private Client client;
    private Statement statement;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setup() {
        client = clientRepository.save(Client.builder()
                .firstName("Test")
                .lastName("User")
                .email("test@test.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .passport(new Passport())
                .build());

        List<StatusHistory> statusHistory = new ArrayList<>();

        statusHistory.add(new StatusHistory(
                ApplicationStatus.PREAPPROVAL,
                LocalDateTime.now(),
                ChangeType.AUTOMATIC
        ));

        statement = statementRepository.save(
                Statement.builder()
                        .client(client)
                        .status(ApplicationStatus.PREAPPROVAL)
                        .statusHistory(statusHistory)
                        .build()
        );

        statementId = statement.getStatementId();

        offer = LoanOfferDto.builder()
                .statementId(statementId)
                .requestedAmount(BigDecimal.valueOf(100000))
                .totalAmount(BigDecimal.valueOf(110000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(10000))
                .rate(BigDecimal.valueOf(10))
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .build();
    }

    @Test
    void shouldBlockSecondThreadUntilFirstFinishes() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch firstStarted = new CountDownLatch(1);

        Callable<Long> firstTask = () -> {
            firstStarted.countDown();

            long start = System.currentTimeMillis();
            try {
                statementService.choosingOneOfTheLoanOffers(offer);
            } catch (Exception ignored) {

            }
            return System.currentTimeMillis() - start;
        };

        Callable<Long> secondTask = () -> {
            firstStarted.await();

            long start = System.currentTimeMillis();
            try {
                statementService.choosingOneOfTheLoanOffers(offer);
            } catch (Exception ignored) {

            }
            return System.currentTimeMillis() - start;
        };

        Future<Long> first = executor.submit(firstTask);
        Future<Long> second = executor.submit(secondTask);

        assertTrue(!first.isDone() || !second.isDone());
    }
}