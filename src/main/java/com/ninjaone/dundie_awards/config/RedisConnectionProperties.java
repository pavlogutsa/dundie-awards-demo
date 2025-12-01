package com.ninjaone.dundie_awards.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisConnectionProperties {
    private String host = "localhost";
    private int port = 6379;
}

