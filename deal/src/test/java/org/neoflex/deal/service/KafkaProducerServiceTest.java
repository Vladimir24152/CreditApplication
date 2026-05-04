package org.neoflex.deal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neoflex.deal.config.KafkaTopicsProperties;
import org.neoflex.deal.dto.EmailMessage;
import org.neoflex.deal.model.enums.Theme;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Тесты сервиса KafkaProducerService")
@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaTopicsProperties kafkaTopicsProperties;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    private EmailMessage emailMessage;
    private UUID statementId;
    private String expectedTopic;

    @BeforeEach
    void setUp() {
        statementId = UUID.randomUUID();
        expectedTopic = "test-topic";

        emailMessage = EmailMessage.builder()
                .address("test@example.com")
                .theme(Theme.FINISH_REGISTRATION)
                .statementId(statementId)
                .text("Test message")
                .build();
    }

    @Test
    @DisplayName("Успешная отправка сообщения в Kafka")
    void sendShouldSendMessageToCorrectTopic() {
        when(kafkaTopicsProperties.getTopic(Theme.FINISH_REGISTRATION)).thenReturn(expectedTopic);

        kafkaProducerService.send(emailMessage);

        ArgumentCaptor<EmailMessage> messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(kafkaTemplate).send(eq(expectedTopic), messageCaptor.capture());

        EmailMessage sentMessage = messageCaptor.getValue();
        assertEquals(emailMessage.getAddress(), sentMessage.getAddress());
        assertEquals(emailMessage.getTheme(), sentMessage.getTheme());
        assertEquals(emailMessage.getStatementId(), sentMessage.getStatementId());
        assertEquals(emailMessage.getText(), sentMessage.getText());

        verify(kafkaTopicsProperties).getTopic(Theme.FINISH_REGISTRATION);
    }

    @Test
    @DisplayName("При неизвестной теме сообщения выбрасывается IllegalArgumentException")
    void sendShouldThrowExceptionWhenTopicNotFound() {
        when(kafkaTopicsProperties.getTopic(Theme.FINISH_REGISTRATION)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,() -> kafkaProducerService.send(emailMessage));

        verify(kafkaTemplate, never()).send(any(), any());
        verify(kafkaTopicsProperties).getTopic(Theme.FINISH_REGISTRATION);
    }
}