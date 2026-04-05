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
import org.neoflex.deal.model.Credit;
import org.neoflex.deal.model.Statement;
import org.neoflex.deal.model.enums.CreditStatus;
import org.neoflex.deal.model.jsonb.StatusHistory;
import org.neoflex.deal.repository.CreditRepository;
import org.neoflex.deal.repository.StatementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.neoflex.deal.model.enums.ApplicationStatus.APPROVED;
import static org.neoflex.deal.model.enums.ChangeType.AUTOMATIC;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditService {

    private final StatementRepository statementRepository;
    private final CreditRepository creditRepository;

    private final CalculatorClientService calculatorClientService;
    private final ClientService clientService;

    private final ClientMapper clientMapper;
    private final CreditMapper creditMapper;

    @Transactional
    public void completeOfRegistrationAndFullCalculation(FinishRegistrationRequestDto request, UUID statementId) {

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
        statement.getStatusHistory().add(new StatusHistory(APPROVED, LocalDateTime.now(), AUTOMATIC));


        log.info("Заявка обновлена: id={}, статус={}", statement.getStatementId(), APPROVED);

        clientService.updateClientPassport(statement.getClient().getClientId(),
                request.getPassportIssueBranch(),
                request.getPassportIssueDate());

        statementRepository.save(statement);
        log.info("Завершение регистрации для заявки {} успешно выполнено", statementId);
    }
}
