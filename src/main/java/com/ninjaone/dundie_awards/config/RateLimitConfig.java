package com.ninjaone.dundie_awards.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rate-limit.write-operations.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitConfig {

    private final RateLimitProperties rateLimitProperties;
    private final RedisConnectionProperties redisConnectionProperties;

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient() {
        String redisUrl = String.format("redis://%s:%d", 
            redisConnectionProperties.getHost(), 
            redisConnectionProperties.getPort());
        log.info("Initializing Redis client for Bucket4j at {}", redisUrl);
        return RedisClient.create(redisUrl);
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<byte[], byte[]> redisConnection(RedisClient redisClient) {
        return redisClient.connect(io.lettuce.core.codec.ByteArrayCodec.INSTANCE);
    }

    @Bean
    @SuppressWarnings("deprecation")
    public ProxyManager<byte[]> proxyManager(StatefulRedisConnection<byte[], byte[]> connection) {
        log.info("Initializing Bucket4j Redis ProxyManager");
        return LettuceBasedProxyManager.builderFor(connection)
            .withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                Duration.ofMinutes(rateLimitProperties.getWindowMinutes() * 2)))
            .build();
    }

    @Bean
    public Supplier<BucketConfiguration> bucketConfigurationSupplier() {
        return () -> {
            Bandwidth limit = Bandwidth.builder()
                .capacity(rateLimitProperties.getRequests())
                .refillIntervally(rateLimitProperties.getRequests(), 
                    Duration.ofMinutes(rateLimitProperties.getWindowMinutes()))
                .build();
            return BucketConfiguration.builder()
                .addLimit(limit)
                .build();
        };
    }

    public int getRequestsPerMinute() {
        return rateLimitProperties.getRequests();
    }

    public int getWindowMinutes() {
        return rateLimitProperties.getWindowMinutes();
    }

    public boolean isEnabled() {
        return rateLimitProperties.isEnabled();
    }
}

