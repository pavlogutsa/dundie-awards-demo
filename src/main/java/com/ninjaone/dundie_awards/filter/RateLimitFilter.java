package com.ninjaone.dundie_awards.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjaone.dundie_awards.config.RateLimitConfig;
import com.ninjaone.dundie_awards.dto.ApiError;
import com.ninjaone.dundie_awards.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.function.Supplier;

@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String GLOBAL_BUCKET_KEY = "global:write:api";
    private static final Set<String> WRITE_METHODS = Set.of(
        HttpMethod.POST.name(),
        HttpMethod.PUT.name(),
        HttpMethod.PATCH.name(),
        HttpMethod.DELETE.name()
    );

    private final ProxyManager<byte[]> proxyManager;
    private final Supplier<BucketConfiguration> bucketConfigurationSupplier;
    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(
            ProxyManager<byte[]> proxyManager,
            Supplier<BucketConfiguration> bucketConfigurationSupplier,
            RateLimitConfig rateLimitConfig,
            ObjectMapper objectMapper) {
        this.proxyManager = proxyManager;
        this.bucketConfigurationSupplier = bucketConfigurationSupplier;
        this.rateLimitConfig = rateLimitConfig;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!rateLimitConfig.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String method = request.getMethod();
        String path = request.getRequestURI();

        // Only apply rate limiting to write operations on /api/* endpoints
        if (isWriteOperation(method) && isApiEndpoint(path)) {
            try {
                byte[] bucketKey = GLOBAL_BUCKET_KEY.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                Bucket bucket = proxyManager.builder()
                    .build(bucketKey, bucketConfigurationSupplier);

                if (bucket.tryConsume(1)) {
                    long availableTokens = bucket.getAvailableTokens();
                    addRateLimitHeaders(response, rateLimitConfig.getRequestsPerMinute(), 
                        availableTokens, bucket);
                    filterChain.doFilter(request, response);
                } else {
                    long availableTokens = bucket.getAvailableTokens();
                    long retryAfterSeconds = calculateRetryAfter(availableTokens, 
                        rateLimitConfig.getWindowMinutes());
                    addRateLimitHeaders(response, rateLimitConfig.getRequestsPerMinute(), 
                        availableTokens, bucket);
                    handleRateLimitExceeded(response, retryAfterSeconds);
                    return;
                }
            } catch (RateLimitExceededException e) {
                handleRateLimitExceeded(response, e.getRetryAfterSeconds());
                return;
            } catch (Exception e) {
                log.error("Error checking rate limit for {} {}", method, path, e);
                // On Redis errors, allow the request through (fail open)
                // In production, you might want to fail closed
                filterChain.doFilter(request, response);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isWriteOperation(String method) {
        return WRITE_METHODS.contains(method);
    }

    private boolean isApiEndpoint(String path) {
        return path != null && path.startsWith("/api/");
    }

    private void addRateLimitHeaders(HttpServletResponse response, int limit, 
                                   long remaining, Bucket bucket) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, remaining)));
        
        // Estimate reset time based on refill rate
        if (remaining < limit) {
            long resetSeconds = rateLimitConfig.getWindowMinutes() * 60;
            response.setHeader("X-RateLimit-Reset", String.valueOf(
                System.currentTimeMillis() / 1000 + resetSeconds));
        }
    }

    private long calculateRetryAfter(long availableTokens, int windowMinutes) {
        if (availableTokens >= 0) {
            return windowMinutes * 60; // Full window
        }
        // Estimate based on refill rate
        return (long) (windowMinutes * 60.0 / rateLimitConfig.getRequestsPerMinute());
    }

    private void handleRateLimitExceeded(HttpServletResponse response, long retryAfterSeconds) 
            throws IOException {
        log.warn("Rate limit exceeded. Retry after {} seconds", retryAfterSeconds);
        
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        
        ApiError apiError = new ApiError(
            HttpStatus.TOO_MANY_REQUESTS.value(),
            "Rate limit exceeded. Too many write requests. Please try again later."
        );
        
        objectMapper.writeValue(response.getWriter(), apiError);
    }
}

