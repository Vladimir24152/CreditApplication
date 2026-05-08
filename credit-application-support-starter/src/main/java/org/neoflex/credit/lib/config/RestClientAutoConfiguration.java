package org.neoflex.credit.lib.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.credit.lib.client.RestClientFactory;
import org.neoflex.credit.lib.interceptor.LoggingInternalInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
@ConditionalOnClass(RestClient.class)
public class RestClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingInternalInterceptor loggingInternalInterceptor() {
        return new LoggingInternalInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestClientFactory restClientFactory(ObjectMapper objectMapper,
                                               LoggingInternalInterceptor loggingInternalInterceptor) {
        return new RestClientFactory(objectMapper, loggingInternalInterceptor);
    }
}