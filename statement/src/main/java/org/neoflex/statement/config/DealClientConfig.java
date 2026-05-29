package org.neoflex.statement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.credit.lib.client.RestClientFactory;
import org.neoflex.statement.client.deal.DealClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DealClientConfig {

    private final String serviceName = "deal service";

    @Value("${client.deal.address:http://localhost:8081}")
    private String serviceUrl;

    private final RestClientFactory restClientFactory;
    private final ConfigurableBeanFactory beanFactory;

    @Bean
    public DealClient dealClient() {
        var restClient = restClientFactory.createRestClient(serviceUrl, serviceName);

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter)
                .embeddedValueResolver(beanFactory::resolveEmbeddedValue)
                .build();

        return factory.createClient(DealClient.class);
    }
}
