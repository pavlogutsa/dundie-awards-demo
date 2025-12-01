package com.ninjaone.dundie_awards.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjaone.dundie_awards.filter.RateLimitFilter;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

@Configuration
@ConditionalOnProperty(name = "rate-limit.write-operations.enabled", havingValue = "true", matchIfMissing = true)
public class FilterConfig {

    @Bean
    public RateLimitFilter rateLimitFilter(
            ProxyManager<byte[]> proxyManager,
            Supplier<BucketConfiguration> bucketConfigurationSupplier,
            RateLimitConfig rateLimitConfig,
            ObjectMapper objectMapper) {
        return new RateLimitFilter(
            proxyManager,
            bucketConfigurationSupplier,
            rateLimitConfig,
            objectMapper
        );
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter filter) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1); // Run early in the filter chain
        registration.setName("rateLimitFilter");
        return registration;
    }
}

