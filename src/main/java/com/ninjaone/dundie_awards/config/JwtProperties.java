package com.ninjaone.dundie_awards.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    @NotBlank(message = "JWT secret must not be blank")
    private String secret = "your-256-bit-secret-key-change-this-in-production-minimum-32-characters";
    
    @Positive(message = "JWT expiration must be positive")
    private long expiration = 86400000; // 24 hours in milliseconds
}

