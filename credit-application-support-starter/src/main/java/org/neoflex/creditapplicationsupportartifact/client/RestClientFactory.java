package org.neoflex.creditapplicationsupportartifact.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.creditapplicationsupportartifact.exception.HttpErrorInternalServiceResponse;
import org.neoflex.creditapplicationsupportartifact.exception.InternalServiceException;
import org.neoflex.creditapplicationsupportartifact.interceptor.LoggingInternalInterceptor;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class RestClientFactory {

    private final ObjectMapper objectMapper;
    private final LoggingInternalInterceptor loggingInternalInterceptor;

    public RestClient createRestClient(String serviceUrl, String serviceName) {
        return RestClient.builder()
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
    }
}