package org.neoflex.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class DetailedLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        long startTime = System.currentTimeMillis();

        return DataBufferUtils.join(request.getBody())
                .defaultIfEmpty(exchange.getResponse()
                        .bufferFactory()
                        .wrap(new byte[0]))
                .flatMap(dataBuffer -> {

                    byte[] requestBodyBytes =
                            new byte[dataBuffer.readableByteCount()];

                    dataBuffer.read(requestBodyBytes);
                    DataBufferUtils.release(dataBuffer);

                    String requestBody =
                            new String(requestBodyBytes, StandardCharsets.UTF_8);

                    URI incomingUri = request.getURI();

                    Flux<DataBuffer> cachedBodyFlux =
                            Flux.defer(() ->
                                    Flux.just(exchange.getResponse()
                                            .bufferFactory()
                                            .wrap(requestBodyBytes)));

                    ServerHttpRequest decoratedRequest =
                            new ServerHttpRequestDecorator(request) {
                                @Override
                                public Flux<DataBuffer> getBody() {
                                    return cachedBodyFlux;
                                }
                            };

                    ServerHttpResponseDecorator decoratedResponse =
                            new ServerHttpResponseDecorator(exchange.getResponse()) {

                                @Override
                                public Mono<Void> writeWith(
                                        Publisher<? extends DataBuffer> body) {

                                    URI targetUri = exchange.getAttribute(
                                            ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR
                                    );

                                    log.info(
                                            ">>> GATEWAY REQUEST, method={}, incomingUrl={}, targetUrl={}, headers={}, requestBody={}",
                                            request.getMethod(),
                                            incomingUri,
                                            targetUri,
                                            request.getHeaders(),
                                            requestBody.isEmpty() ? "[empty]" : requestBody
                                    );

                                    if (body instanceof Flux<?> fluxBody) {

                                        return super.writeWith(
                                                ((Flux<? extends DataBuffer>) fluxBody)
                                                        .buffer()
                                                        .map(dataBuffers -> {

                                                            int totalSize =
                                                                    dataBuffers.stream()
                                                                            .mapToInt(DataBuffer::readableByteCount)
                                                                            .sum();

                                                            byte[] responseBytes =
                                                                    new byte[totalSize];

                                                            int offset = 0;

                                                            for (DataBuffer db : dataBuffers) {

                                                                byte[] bytes =
                                                                        new byte[db.readableByteCount()];

                                                                db.read(bytes);

                                                                System.arraycopy(
                                                                        bytes,
                                                                        0,
                                                                        responseBytes,
                                                                        offset,
                                                                        bytes.length
                                                                );

                                                                offset += bytes.length;

                                                                DataBufferUtils.release(db);
                                                            }

                                                            String responseBody =
                                                                    new String(
                                                                            responseBytes,
                                                                            StandardCharsets.UTF_8
                                                                    );

                                                            long duration =
                                                                    System.currentTimeMillis()
                                                                            - startTime;

                                                            log.info(
                                                                    "<<< GATEWAY RESPONSE, status={}, headers={}, contentType={}, durationMs={}, responseBody={}",
                                                                    getStatusCode(),
                                                                    getHeaders(),
                                                                    getHeaders().getContentType(),
                                                                    duration,
                                                                    responseBody.isEmpty() ? "[empty]" : responseBody
                                                            );

                                                            return exchange.getResponse()
                                                                    .bufferFactory()
                                                                    .wrap(responseBytes);
                                                        })
                                        );
                                    }

                                    return super.writeWith(body);
                                }
                            };

                    return chain.filter(
                            exchange.mutate()
                                    .request(decoratedRequest)
                                    .response(decoratedResponse)
                                    .build()
                    );
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}