package org.neoflex.deal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.deal.client.calculator.CalculatorClientService;
import org.neoflex.deal.dto.ClientDto;
import org.neoflex.deal.dto.CreditDto;
import org.neoflex.deal.dto.DealDocumentDto;
import org.neoflex.deal.dto.EmailMessage;
import org.neoflex.deal.dto.LoanOfferDto;
import org.neoflex.deal.dto.LoanStatementRequestDto;
import org.neoflex.deal.dto.StatementResponseDto;
import org.neoflex.deal.mapper.ClientMapper;
import org.neoflex.deal.mapper.CreditMapper;
import org.neoflex.deal.mapper.StatementMapper;
import org.neoflex.deal.model.Client;
import org.neoflex.deal.model.Credit;
import org.neoflex.deal.model.Statement;
import org.neoflex.deal.model.jsonb.StatusHistory;
import org.neoflex.deal.producer.KafkaProducerService;
import org.neoflex.deal.repository.ClientRepository;
import org.neoflex.deal.repository.StatementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.neoflex.deal.model.enums.ApplicationStatus.APPROVED;
import static org.neoflex.deal.model.enums.ApplicationStatus.PREAPPROVAL;
import static org.neoflex.deal.model.enums.ChangeType.AUTOMATIC;
import static org.neoflex.deal.model.enums.Theme.FINISH_REGISTRATION;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatementService {

    private final ClientRepository clientRepository;
    private final StatementRepository statementRepository;

    private final CalculatorClientService calculatorClientService;
    private final KafkaProducerService kafkaProducerService;

    private final ClientMapper clientMapper;
    private final StatementMapper statementMapper;

    @Transactional
    public List<LoanOfferDto> calculateTerms(@NonNull LoanStatementRequestDto request) {

        Client savedClient = clientRepository.save(clientMapper.toClient(request));
        log.info("Клиент сохранен: clientId={}", savedClient.getClientId());

        Statement statement = Statement.builder()
                .status(PREAPPROVAL)
                .statusHistory(List.of(StatusHistory.builder()
                        .status(PREAPPROVAL)
                        .time(LocalDateTime.now())
                        .changeType(AUTOMATIC)
                        .build()))
                .client(savedClient)
                .build();

        Statement savedStatement = statementRepository.save(statement);

        log.info("Заявка создана: statementId={}, статус={}, clientId={}",
                savedStatement.getStatementId(), PREAPPROVAL, savedClient.getClientId());


        List<LoanOfferDto> offers = calculatorClientService.calculateLoanOffers(request);

        offers.forEach(offer -> offer.setStatementId(savedStatement.getStatementId()));

        return offers;
    }

    @Transactional
    public void selectOffer(@NonNull LoanOfferDto request) {

        Statement statement = statementRepository.findById(request.getStatementId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Не найдена заявка с id указанным в запросе: %s",request.getStatementId()))
                );

        statement.setStatus(APPROVED);

        List<StatusHistory> statusHistory = statement.getStatusHistory();
        statusHistory.add(new StatusHistory(APPROVED, LocalDateTime.now(), AUTOMATIC));
        statement.setStatusHistory(statusHistory);

        statement.setAppliedOffer(request);

        statementRepository.save(statement);
        log.info("Заявка {} успешно обновлена: статус = {}", request.getStatementId(), APPROVED);

        EmailMessage emailMessage = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(FINISH_REGISTRATION)
                .statementId(statement.getStatementId())
                .text("Ваша заявка предварительно одобрена, завершите оформление.")
                .build();

        kafkaProducerService.send(emailMessage);
    }

    @Transactional(readOnly = true)
    public DealDocumentDto getInfo(UUID statementId) {

        Statement statement = statementRepository.findByIdWithDetailsReadOnly(statementId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Не найдена заявка с id указанным в запросе: %s",statementId))
                );

        return statementMapper.toDealDocumentDto(statement, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public StatementResponseDto get(UUID statementId) {

        Statement statement = statementRepository.findFullStatementById(statementId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Не найдена заявка с id указанным в запросе: %s",statementId))
                );

        return statementMapper.toStatementResponseDto(statement);
    }

    @Transactional(readOnly = true)
    public List<StatementResponseDto> getAll() {
        List<Statement> statements = statementRepository.findAllFullStatementById();
        if (statements == null) {
            return new ArrayList<>();
        }
        return statements.stream()
                .map(statementMapper::toStatementResponseDto)
                .collect(Collectors.toList());
    }
}
