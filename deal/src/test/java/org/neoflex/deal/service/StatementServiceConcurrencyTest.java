package org.neoflex.deal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@Testcontainers
@DisplayName("Тесты сервиса StatementService на конкурентный доступ к обновлению ресурса")
class StatementServiceConcurrencyTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private StatementService statementService;

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private ClientRepository clientRepository;

    @MockitoBean
    private KafkaProducerService kafkaProducerService;

    private UUID statementId;
    private LoanOfferDto offer;
    private Client client;
    private Statement statement;

    private final Long EPSILON = 5L;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setup() {
        doNothing().when(kafkaProducerService).send(any());

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
    @DisplayName("Время выполнения методов должно отличаться из-за блокировки БД")
    void shouldBlockSecondThreadUntilFirstFinishes() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicLong startTime1 = new AtomicLong();
        AtomicLong startTime2 = new AtomicLong();
        AtomicLong endTime1 = new AtomicLong();
        AtomicLong endTime2 = new AtomicLong();

        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(() -> {
            countDownLatch.countDown();
            startTime1.set(System.currentTimeMillis());
            statementService.selectOffer(offer);
            endTime1.set(System.currentTimeMillis());
            return endTime1.get() - startTime1.get();
        }, executor);

        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(() -> {
            countDownLatch.countDown();
            startTime2.set(System.currentTimeMillis());
            statementService.selectOffer(offer);
            endTime2.set(System.currentTimeMillis());
            return endTime2.get() - startTime2.get();
        }, executor);

        CompletableFuture.allOf(f1, f2).join();

        assertTrue(Math.abs(f1.get() - f2.get()) > EPSILON);
    }
}