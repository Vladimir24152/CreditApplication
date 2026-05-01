package org.neoflex.deal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.creditapplicationsupportstarter.dto.EmailMessage;
import org.neoflex.deal.exception.CodeVerificationException;
import org.neoflex.deal.model.Statement;
import org.neoflex.deal.repository.StatementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

import static org.neoflex.creditapplicationsupportstarter.enums.Theme.CREDIT_ISSUED;
import static org.neoflex.creditapplicationsupportstarter.enums.Theme.SEND_DOCUMENTS;
import static org.neoflex.creditapplicationsupportstarter.enums.Theme.SEND_SES;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final StatementRepository statementRepository;
    private final KafkaProducerService kafkaProducerService;

    // заменить на БД!!!!!
    private final java.util.Map<String, String> verificationCodes = new java.util.concurrent.ConcurrentHashMap<>();

    private static final SecureRandom random = new SecureRandom();


    @Transactional
    public void sendDocuments(@NonNull UUID statementId) {
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Не найдена заявка с id указанным в запросе: %s",statementId)));

        EmailMessage message = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(SEND_DOCUMENTS)
                .statementId(statementId.getMostSignificantBits())
                .text("Документы для подписания готовы. Пожалуйста, ознакомьтесь и подпишите.")
                .build();

        kafkaProducerService.send(message);
    }

    @Transactional
    public void signDocuments(@NonNull UUID statementId) {
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Не найдена заявка с id указанным в запросе: %s",statementId)));

        String code = String.format("%06d", random.nextInt(1000000));
        verificationCodes.put(String.valueOf(statementId.getMostSignificantBits()), code);

        EmailMessage message = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(SEND_SES)
                .statementId(statementId.getMostSignificantBits())
                .text(code)
                .build();

        kafkaProducerService.send(message);
    }

    @Transactional
    public void verifyCode(@NonNull UUID statementId, String code) {
        String expectedCode = verificationCodes.get(statementId);

        if (!expectedCode.equals(code)) {
            throw new CodeVerificationException("Неверный код для заявки: " + statementId);
        }

        verificationCodes.remove(statementId);

        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Не найдена заявка с id указанным в запросе: %s",statementId)));

        EmailMessage message = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(CREDIT_ISSUED)
                .statementId(statementId.getMostSignificantBits())
                .text("Кредит успешно выдан.")
                .build();

        kafkaProducerService.send(message);
    }
}
