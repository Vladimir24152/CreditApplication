package org.neoflex.deal.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.deal.config.KafkaTopicsProperties;
import org.neoflex.deal.dto.EmailMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, EmailMessage> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    public void send(EmailMessage message) {

        String topic = kafkaTopicsProperties.getTopic(message.getTheme());

        if (topic == null) {
            log.error("Тема сообщения неизвестна: {}, невозможно отправить сообщение", message.getTheme());
            throw new IllegalArgumentException("Тема сообщения неизвестна: " + message.getTheme());
        }

        log.info("Отправка сообщения в топик {}: {}", topic, message);
        kafkaTemplate.send(topic, message);
    }
}
