package org.neoflex.deal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.deal.client.calculator.CalculatorClient;
import org.neoflex.deal.dto.response.HttpErrorInternalServiceResponse;
import org.neoflex.deal.exception.InternalServiceException;
import org.neoflex.deal.interceptor.LoggingInnerInterceptor;
import org.springframework.beans.factory.annotation.Value;
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
public class CalculatorClientConfig {

    @Value("${calculator.service.url:http://localhost:8080}")
    private String calculatorServiceUrl;

    private final String serviceName = "calculator service";

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private final LoggingInnerInterceptor loggingInnerInterceptor;


    @Bean
    public CalculatorClient calculatorClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(calculatorServiceUrl)
                .requestInterceptor(loggingInnerInterceptor)
                .defaultStatusHandler(HttpStatusCode::isError,
                        (request, response) -> {
                            HttpErrorInternalServiceResponse errorResponse = null;
                            String message;

                            try {
                                String errorBody = new String(response.getBody().readAllBytes(),StandardCharsets.UTF_8);

                                errorResponse = objectMapper.readValue(errorBody,
                                        HttpErrorInternalServiceResponse.class);

                                log.error("RAW ERROR BODY: {}", errorBody);

                                message = String.format("Произошла внутренняя ошибка сервера: Ошибка при вызове %s",serviceName);
                            } catch (Exception e) {
                                message = "Не удалось прочитать тело ошибки";
                            }

                            throw new InternalServiceException(serviceName,message,errorResponse);
                        })
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(CalculatorClient.class);
    }
}
