package org.neoflex.deal.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic finishRegistrationTopic() {
        return TopicBuilder.name("finish-registration")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic createDocumentsTopic() {
        return TopicBuilder.name("create-documents")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic sendDocumentsTopic() {
        return TopicBuilder.name("send-documents")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic sendSesTopic() {
        return TopicBuilder.name("send-ses")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic creditIssuedTopic() {
        return TopicBuilder.name("credit-issued")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic statementDeniedTopic() {
        return TopicBuilder.name("statement-denied")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
