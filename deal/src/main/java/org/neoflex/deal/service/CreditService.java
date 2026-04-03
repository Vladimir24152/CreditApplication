package org.neoflex.deal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.deal.client.calculator.CalculatorClientService;
import org.neoflex.deal.dto.CreditDto;
import org.neoflex.deal.dto.FinishRegistrationRequestDto;
import org.neoflex.deal.dto.ScoringDataDto;
import org.neoflex.deal.mapper.ClientMapper;
import org.neoflex.deal.mapper.CreditMapper;
import org.neoflex.deal.model.Client;
import org.neoflex.deal.model.Credit;
import org.neoflex.deal.model.Statement;
import org.neoflex.deal.model.enums.CreditStatus;
import org.neoflex.deal.model.jsonb.Passport;
import org.neoflex.deal.model.jsonb.StatusHistory;
import org.neoflex.deal.repository.ClientRepository;
import org.neoflex.deal.repository.CreditRepository;
import org.neoflex.deal.repository.StatementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.neoflex.deal.model.enums.ApplicationStatus.APPROVED;
import static org.neoflex.deal.model.enums.ChangeType.AUTOMATIC;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditService {

    private final StatementRepository statementRepository;
    private final CreditRepository creditRepository;
    private final ClientRepository clientRepository;

    private final CalculatorClientService calculatorClientService;

    private final ClientMapper clientMapper;
    private final CreditMapper creditMapper;

    @Transactional
    public void completionOfRegistrationAndFullCreditCalculation(FinishRegistrationRequestDto request, UUID statementId){

        log.info("Завершение регистрации для заявки: {}", statementId);

        if (request == null) {
            throw new NullPointerException("Отсутствует тело запроса");
        }

        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Не найдена заявка с id указанным в запросе: %s",statementId))
                );

        ScoringDataDto scoringDataDto = clientMapper.toScoringDataDto(request,statement);

        CreditDto creditDto = calculatorClientService.calculateCredit(scoringDataDto);

        Credit credit = creditRepository.save(creditMapper.toCredit(creditDto, CreditStatus.CALCULATED));

        statement.setCredit(credit);
        log.info("Сохранен кредит: id={}, статус={}", credit.getCreditId(), credit.getCreditStatus());

        statement.setStatus(APPROVED);
        List<StatusHistory> statusHistory = statement.getStatusHistory();
        statusHistory.add(new StatusHistory(APPROVED, LocalDateTime.now(), AUTOMATIC));
        statement.setStatusHistory(statusHistory);

        log.info("Заявка обновлена: статус={}", APPROVED);

        updateClientFromRequest(statement, request.getPassportIssueBranch(), request.getPassportIssueDate());

        statementRepository.save(statement);
        log.info("Завершение регистрации для заявки {} успешно выполнено", statementId);
    }

    private void updateClientFromRequest(Statement statement, String issueBranch, LocalDate issueDate) {

        Client client = clientRepository.findById(statement.getClient().getClientId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Не найдена заявка с id указанным в заявке: %s", statement.getClient().getClientId()))
                );

        Passport passport = client.getPassport();
        passport.setIssueBranch(issueBranch);
        passport.setIssueDate(issueDate);
        client.setPassport(passport);

        clientRepository.save(client);

        log.info("Обновлен паспорт клиента: id={}, issueBranch={}, issueDate={}",
                client.getClientId(), issueBranch, issueDate);
    }
}
