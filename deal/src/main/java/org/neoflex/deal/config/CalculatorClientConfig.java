package org.neoflex.deal.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.creditapplicationsupportartifact.client.RestClientFactory;
import org.neoflex.deal.client.calculator.CalculatorClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CalculatorClientConfig {

    @Value("${calculator.service.url:http://localhost:8080}")
    private String serviceUrl;

    private final String serviceName = "calculator service";

    private final RestClientFactory restClientFactory;
    private final ConfigurableBeanFactory beanFactory;


    @Bean
    public CalculatorClient dealClient() {
        var restClient = restClientFactory.createRestClient(serviceUrl, serviceName);

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter)
                .embeddedValueResolver(beanFactory::resolveEmbeddedValue)
                .build();

        return factory.createClient(CalculatorClient.class);
    }
}
