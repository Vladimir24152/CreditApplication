package org.neoflex.statement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.statement.client.deal.DealClient;
import org.neoflex.statement.dto.response.HttpErrorInternalServiceResponse;
import org.neoflex.statement.exception.InternalServiceException;
import org.neoflex.statement.interceptor.LoggingInternalInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DealClientConfig {

    private final String serviceName = "deal service";

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Value("${client.deal.address:http://localhost:8081}")
    private String serviceUrl;

    private final LoggingInternalInterceptor loggingInternalInterceptor;
    private final ConfigurableBeanFactory beanFactory;

    @Bean
    public DealClient dealClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(serviceUrl)
                .requestInterceptor(loggingInternalInterceptor)
                .defaultStatusHandler(HttpStatusCode::isError,
                        (request, response) -> {
                            HttpErrorInternalServiceResponse errorResponse = null;
                            String message;

                            try {
                                String errorBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

                                errorResponse = objectMapper.readValue(errorBody,
                                        HttpErrorInternalServiceResponse.class);

                                log.error("RAW ERROR BODY: {}", errorBody);

                                message = String.format("Произошла внутренняя ошибка сервера: Ошибка при вызове %s", serviceName);
                            } catch (Exception e) {
                                message = "Не удалось прочитать тело ошибки";
                            }

                            throw new InternalServiceException(serviceName, message, errorResponse);
                        })
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter)
                .embeddedValueResolver(beanFactory::resolveEmbeddedValue)
                .build();

        return factory.createClient(DealClient.class);
    }
}
