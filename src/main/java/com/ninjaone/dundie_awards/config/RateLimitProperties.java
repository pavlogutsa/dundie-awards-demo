package com.ninjaone.dundie_awards.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rate-limit.write-operations")
public class RateLimitProperties {
    private int requests = 100;
    private int windowMinutes = 1;
    private boolean enabled = true;
}

