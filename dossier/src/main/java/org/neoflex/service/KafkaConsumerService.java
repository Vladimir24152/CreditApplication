package org.neoflex.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.neoflex.creditapplicationsupportstarter.dto.EmailMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final EmailService emailService;

    @KafkaListener(topics = "finish-registration", groupId = "dossier-group")
    public void handleFinishRegistration(EmailMessage message) {
        log.info("Получено сообщение по теме {}: statementId={}",message.getTheme(),message.getStatementId());
        emailService.sendEmail(message);
    }

    @KafkaListener(topics = "create-documents", groupId = "dossier-group")
    public void handleCreateDocuments(EmailMessage message) {
        log.info("Получено сообщение по теме {}: statementId={}",message.getTheme(),message.getStatementId());
        emailService.sendEmail(message);
    }

    @KafkaListener(topics = "send-documents", groupId = "dossier-group")
    public void handleSendDocuments(EmailMessage message) {
        log.info("Получено сообщение по теме {}: statementId={}",message.getTheme(),message.getStatementId());
        emailService.sendEmail(message);
    }

    @KafkaListener(topics = "send-ses", groupId = "dossier-group")
    public void handleSendSes(EmailMessage message) {
        log.info("Получено сообщение по теме {}: statementId={}",message.getTheme(),message.getStatementId());
        emailService.sendEmail(message);
    }

    @KafkaListener(topics = "credit-issued", groupId = "dossier-group")
    public void handleCreditIssued(EmailMessage message) {
        log.info("Получено сообщение по теме {}: statementId={}",message.getTheme(),message.getStatementId());
        emailService.sendEmail(message);
    }

    @KafkaListener(topics = "statement-denied", groupId = "dossier-group")
    public void handleStatementDenied(EmailMessage message) {
        log.info("Получено сообщение по теме {}: statementId={}",message.getTheme(),message.getStatementId());
        emailService.sendEmail(message);
    }
}
