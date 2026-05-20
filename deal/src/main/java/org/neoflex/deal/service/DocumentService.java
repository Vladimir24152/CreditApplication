package org.neoflex.deal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.deal.dto.EmailMessage;
import org.neoflex.deal.exception.CodeVerificationException;
import org.neoflex.deal.model.Credit;
import org.neoflex.deal.model.Statement;
import org.neoflex.deal.model.enums.ApplicationStatus;
import org.neoflex.deal.model.enums.CreditStatus;
import org.neoflex.deal.producer.KafkaProducerService;
import org.neoflex.deal.repository.CreditRepository;
import org.neoflex.deal.repository.StatementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.neoflex.deal.model.enums.ApplicationStatus.APPROVED;
import static org.neoflex.deal.model.enums.ApplicationStatus.DOCUMENT_CREATED;
import static org.neoflex.deal.model.enums.ApplicationStatus.DOCUMENT_SIGNED;
import static org.neoflex.deal.model.enums.ApplicationStatus.PREPARE_DOCUMENTS;
import static org.neoflex.deal.model.enums.Theme.CREDIT_ISSUED;
import static org.neoflex.deal.model.enums.Theme.SEND_DOCUMENTS;
import static org.neoflex.deal.model.enums.Theme.SEND_SES;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final StatementRepository statementRepository;
    private final CreditRepository creditRepository;
    private final KafkaProducerService kafkaProducerService;

    private static final SecureRandom random = new SecureRandom();


    @Transactional
    public void sendDocuments(@NonNull UUID statementId) {
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Не найдена заявка с id указанным в запросе: %s",statementId)));

        statement.setStatus(PREPARE_DOCUMENTS);
        statementRepository.save(statement);

        EmailMessage message = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(SEND_DOCUMENTS)
                .statementId(statementId)
                .text("Документы для подписания готовы. Пожалуйста, ознакомьтесь и подпишите.")
                .build();

        kafkaProducerService.send(message);
    }

    @Transactional
    public void signDocuments(@NonNull UUID statementId) {
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Не найдена заявка с id указанным в запросе: %s",statementId)));

        if (statement.getStatus() != PREPARE_DOCUMENTS) {
            throw new IllegalStateException(
                    String.format("Некорректный статус заявки %s для завершения регистрации. Ожидаемый статус %s," +
                                    " текущий статус: %s",
                            statementId, DOCUMENT_CREATED, statement.getStatus())
            );
        }

        String code = String.format("%06d", random.nextInt(1000000));
        statement.setSesCode(code);
        statement.setStatus(DOCUMENT_CREATED);
        statementRepository.save(statement);

        EmailMessage message = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(SEND_SES)
                .statementId(statementId)
                .text(code)
                .build();

        kafkaProducerService.send(message);
    }

    @Transactional
    public void verifyCode(@NonNull UUID statementId,@NonNull String code) {

        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Не найдена заявка с id указанным в запросе: %s",statementId)));

        if (statement.getStatus() != DOCUMENT_CREATED) {
            throw new IllegalStateException(
                    String.format("Некорректный статус заявки %s для завершения регистрации. Ожидаемый статус %s," +
                                    " текущий статус: %s",
                            statementId, DOCUMENT_CREATED, statement.getStatus())
            );
        }

        String expectedCode = statement.getSesCode();

        if (!expectedCode.equals(code)) {
            throw new CodeVerificationException("Неверный SES код для заявки: " + statementId);
        }

        Credit credit = statement.getCredit();
        credit.setCreditStatus(CreditStatus.ISSUED);
        statement.setStatus(ApplicationStatus.CREDIT_ISSUED);
        statement.setSignDate(LocalDateTime.now());

        creditRepository.save(credit);
        statementRepository.save(statement);

        EmailMessage message = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(CREDIT_ISSUED)
                .statementId(statementId)
                .text("Кредит успешно выдан.")
                .build();

        kafkaProducerService.send(message);
    }
}
