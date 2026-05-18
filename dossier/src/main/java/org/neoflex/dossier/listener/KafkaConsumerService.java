package org.neoflex.dossier.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.dossier.dto.EmailMessage;
import org.neoflex.dossier.service.DocumentService;
import org.neoflex.dossier.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final EmailService emailService;
    private final DocumentService documentService;

    @KafkaListener(topics = "finish-registration", groupId = "dossier-group")
    public void handleFinishRegistration(EmailMessage message) {
        log.info("Получено сообщение в топик finish-registration: statementId={}, theme={}",
                message.getStatementId(), message.getTheme());
        emailService.sendEmail(message);
    }

    @KafkaListener(topics = "create-documents", groupId = "dossier-group")
    public void handleCreateDocuments(EmailMessage message) {
        log.info("Получено сообщение в топик create-documents: statementId={}, theme={}",
                message.getStatementId(), message.getTheme());
        emailService.sendEmail(message);
    }

    @KafkaListener(topics = "send-documents", groupId = "dossier-group")
    public void handleSendDocuments(EmailMessage message) {
        log.info("Получено сообщение в топик send-documents: statementId={}, theme={}",
                message.getStatementId(), message.getTheme());
        documentService.createDocument(message);
    }

    @KafkaListener(topics = "send-ses", groupId = "dossier-group")
    public void handleSendSes(EmailMessage message) {
        log.info("Получено сообщение в топик send-ses: statementId={}, theme={}",
                message.getStatementId(), message.getTheme());
        emailService.sendEmail(message);
    }

    @KafkaListener(topics = "credit-issued", groupId = "dossier-group")
    public void handleCreditIssued(EmailMessage message) {
        log.info("Получено сообщение в топик credit-issued: statementId={}, theme={}",
                message.getStatementId(), message.getTheme());
        emailService.sendEmail(message);
    }

    @KafkaListener(topics = "statement-denied", groupId = "dossier-group")
    public void handleStatementDenied(EmailMessage message) {
        log.info("Получено сообщение в топик statement-denied: statementId={}, theme={}",
                message.getStatementId(), message.getTheme());
        emailService.sendEmail(message);
    }
}
