package org.neoflex.credit.lib.config;

import lombok.extern.slf4j.Slf4j;
import org.neoflex.credit.lib.interceptor.LoggingInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "credit.support.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggingAutoConfiguration implements WebMvcConfigurer {

    @Bean
    @ConditionalOnMissingBean
    public LoggingInterceptor loggingInterceptor() {
        return new LoggingInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor())
                .addPathPatterns("/api/**");
    }
}