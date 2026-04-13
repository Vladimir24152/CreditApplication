package org.neoflex.deal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.deal.client.calculator.CalculatorClientService;
import org.neoflex.deal.dto.LoanOfferDto;
import org.neoflex.deal.dto.LoanStatementRequestDto;
import org.neoflex.deal.mapper.ClientMapper;
import org.neoflex.deal.model.Client;
import org.neoflex.deal.model.Statement;
import org.neoflex.deal.model.jsonb.StatusHistory;
import org.neoflex.deal.repository.ClientRepository;
import org.neoflex.deal.repository.StatementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.neoflex.deal.model.enums.ApplicationStatus.APPROVED;
import static org.neoflex.deal.model.enums.ApplicationStatus.PREAPPROVAL;
import static org.neoflex.deal.model.enums.ChangeType.AUTOMATIC;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatementService {

    private final ClientRepository clientRepository;
    private final StatementRepository statementRepository;

    private final CalculatorClientService calculatorClientService;

    private final ClientMapper clientMapper;

    @Transactional
    public List<LoanOfferDto> calculateTerms(LoanStatementRequestDto request) {

        if (request == null) {
            throw new NullPointerException("Отсутствует тело запроса");
        }

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
    public void selectOffer(LoanOfferDto request) {

        if (request == null) {
            throw new NullPointerException("Отсутствует тело запроса");
        }

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
    }
}
