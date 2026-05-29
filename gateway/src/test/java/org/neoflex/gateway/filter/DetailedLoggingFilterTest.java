package org.neoflex.gateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

class DetailedLoggingFilterTest {

    @Test
    @DisplayName("Фильтр должен вызывать следующий фильтр в цепочке")
    void filterShouldCallNextFilter() {
        DetailedLoggingFilter filter = new DetailedLoggingFilter();
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://localhost:8084/test")
                        .build()
        );

        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    @DisplayName("Фильтр должен иметь порядок -1")
    void filterOrderShouldBeMinusOne() {
        DetailedLoggingFilter filter = new DetailedLoggingFilter();

        int order = filter.getOrder();

        assert order == -1;
    }
}