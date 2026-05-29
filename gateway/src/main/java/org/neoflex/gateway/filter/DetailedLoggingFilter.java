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
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        return extractRequestBody(exchange)
                .flatMap(requestBodyBytes -> {
                    String requestBody = new String(requestBodyBytes, StandardCharsets.UTF_8);
                    URI incomingUri = request.getURI();

                    ServerHttpRequest decoratedRequest = decorateRequest(exchange, requestBodyBytes);
                    ServerHttpResponseDecorator decoratedResponse = decorateResponse(exchange, request, incomingUri, requestBody, startTime);

                    return chain.filter(exchange.mutate()
                            .request(decoratedRequest)
                            .response(decoratedResponse)
                            .build());
                });
    }

    private Mono<byte[]> extractRequestBody(ServerWebExchange exchange) {
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                });
    }

    private ServerHttpRequest decorateRequest(ServerWebExchange exchange, byte[] requestBodyBytes) {
        Flux<DataBuffer> cachedBodyFlux = Flux.defer(() ->
                Flux.just(exchange.getResponse().bufferFactory().wrap(requestBodyBytes)));

        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public Flux<DataBuffer> getBody() {
                return cachedBodyFlux;
            }
        };
    }

    private ServerHttpResponseDecorator decorateResponse(ServerWebExchange exchange,
                                                         ServerHttpRequest request,
                                                         URI incomingUri,
                                                         String requestBody,
                                                         long startTime) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                logRequest(request, incomingUri, requestBody);
                return writeResponse(body);
            }

            private void logRequest(ServerHttpRequest request, URI incomingUri, String requestBody) {
                URI targetUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
                log.info(">>> GATEWAY REQUEST, method={}, incomingUrl={}, targetUrl={}, headers={}, requestBody={}",
                        request.getMethod(), incomingUri, targetUri, request.getHeaders(),
                        requestBody.isEmpty() ? "[empty]" : requestBody);
            }

            private Mono<Void> writeResponse(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux<?>) {
                    return super.writeWith(extractAndLogResponse((Flux<? extends DataBuffer>) body));
                }
                return super.writeWith(body);
            }

            private Publisher<? extends DataBuffer> extractAndLogResponse(Flux<? extends DataBuffer> fluxBody) {
                return fluxBody.buffer().map(dataBuffers -> {
                    byte[] responseBytes = accumulateResponseBytes(dataBuffers);
                    logResponse(responseBytes);
                    return exchange.getResponse().bufferFactory().wrap(responseBytes);
                });
            }

            private byte[] accumulateResponseBytes(java.util.List<? extends DataBuffer> dataBuffers) {
                int totalSize = dataBuffers.stream().mapToInt(DataBuffer::readableByteCount).sum();
                byte[] responseBytes = new byte[totalSize];
                int offset = 0;

                for (DataBuffer db : dataBuffers) {
                    byte[] bytes = new byte[db.readableByteCount()];
                    db.read(bytes);
                    System.arraycopy(bytes, 0, responseBytes, offset, bytes.length);
                    offset += bytes.length;
                    DataBufferUtils.release(db);
                }
                return responseBytes;
            }

            private void logResponse(byte[] responseBytes) {
                long duration = System.currentTimeMillis() - startTime;
                String responseBody = new String(responseBytes, StandardCharsets.UTF_8);
                log.info("<<< GATEWAY RESPONSE, status={}, headers={}, contentType={}, durationMs={}, responseBody={}",
                        getStatusCode(), getHeaders(), getHeaders().getContentType(),
                        duration, responseBody.isEmpty() ? "[empty]" : responseBody);
            }
        };
    }

    @Override
    public int getOrder() {
        return -1;
    }
}