package org.neoflex.deal.config;

import org.neoflex.creditapplicationsupportstarter.enums.Theme;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("kafka-topics")
public class KafkaTopicsProperties {

    private String finishRegistration = "finish-registration";
    private String createDocuments = "create-documents";
    private String sendDocuments = "send-documents";
    private String sendSes = "send-ses";
    private String creditIssued = "credit-issued";
    private String statementDenied = "statement-denied";

    public String getTopic(Theme theme) {
        return switch (theme) {
            case FINISH_REGISTRATION -> finishRegistration;
            case CREATE_DOCUMENTS -> createDocuments;
            case SEND_DOCUMENTS -> sendDocuments;
            case SEND_SES -> sendSes;
            case CREDIT_ISSUED -> creditIssued;
            case STATEMENT_DENIED -> statementDenied;
        };
    }
}
