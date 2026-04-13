package org.neoflex.statement.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
        byte[] responseBody = StreamUtils.copyToByteArray(response.getBody());

        String responseBodyString = new String(responseBody, StandardCharsets.UTF_8);

        log.info(">>> Получен ответ от внешнего сервиса: Status = {}, Headers = {}, Response Body = {}",
                response.getStatusCode(),
                response.getHeaders(),
                responseBodyString
        );

        return new BufferingClientHttpResponse(response, responseBody);
    }
}
