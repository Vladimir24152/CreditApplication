package org.neoflex.statement.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LoggingInnerInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        log.info("<<< Отправка запроса во внешний сервис: URL = {} {}, Headers = {}, Request Body = {}",
                request.getMethod(), request.getURI(),
                request.getHeaders(),
                new String(body, StandardCharsets.UTF_8)
        );

        ClientHttpResponse response = execution.execute(request, body);

        String responseBody = new BufferedReader(
                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        log.info(">>> Получен ответ от внешнего сервиса: Status = {}, Headers = {}, Request Body = {}",
                response.getStatusCode(),
                response.getHeaders(),
                responseBody
        );

        return response;
    }
}
