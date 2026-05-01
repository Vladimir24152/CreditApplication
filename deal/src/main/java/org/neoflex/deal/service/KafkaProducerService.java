package org.neoflex.deal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.creditapplicationsupportstarter.dto.EmailMessage;

import org.neoflex.creditapplicationsupportstarter.enums.Theme;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String,Object> kafkaTemplate;

    private static final Map<Theme,String> TOPIC_MAP = new HashMap<>();

    static {
        TOPIC_MAP.put(Theme.FINISH_REGISTRATION, "finish-registration");
        TOPIC_MAP.put(Theme.CREATE_DOCUMENTS, "create-documents");
        TOPIC_MAP.put(Theme.SEND_DOCUMENTS, "send-documents");
        TOPIC_MAP.put(Theme.SEND_SES, "send-ses");
        TOPIC_MAP.put(Theme.CREDIT_ISSUED, "credit-issued");
        TOPIC_MAP.put(Theme.STATEMENT_DENIED, "statement-denied");
    }

    public void send(EmailMessage message) {

        String topic = TOPIC_MAP.get(message.getTheme());

        if (topic == null) {
            log.error("Тема сообщения неизвестна: {}, невозможно отправить сообщение", message.getTheme());
            throw new IllegalArgumentException("Тема сообщения неизвестна: " + message.getTheme());
        }

        log.info("Отправка сообщения в топик{}: {}", topic, message);
        kafkaTemplate.send(topic, message);
    }
}
