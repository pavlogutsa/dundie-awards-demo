package com.ninjaone.dundie_awards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjaone.dundie_awards.dto.ApiError;
import com.ninjaone.dundie_awards.dto.EmployeeRequest;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import lombok.NonNull;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
@TestPropertySource(properties = "rate-limit.write-operations.enabled=true")
@Import(com.ninjaone.dundie_awards.config.TestSecurityConfig.class)
@SuppressWarnings("null")
class RateLimitIntegrationTest {

    @Container
    @SuppressWarnings("resource") // TestContainers manages lifecycle automatically via @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired(required = false)
    private ProxyManager<byte[]> proxyManager;
    
    @Autowired(required = false)
    private Supplier<BucketConfiguration> bucketConfigurationSupplier;

    @BeforeEach
    void setUp() {
        activityRepository.deleteAll();
        employeeRepository.deleteAll();
        organizationRepository.deleteAll();
        
        // Clear rate limit bucket from Redis to ensure clean test state
        // Bucket4j stores keys as bytes, so we need to use ProxyManager to reset
        if (proxyManager != null && bucketConfigurationSupplier != null) {
            try {
                byte[] bucketKey = "global:write:api".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                // Remove the existing bucket by creating a new one which will overwrite
                proxyManager.removeProxy(bucketKey);
            } catch (Exception e) {
                // Ignore if bucket doesn't exist yet
            }
        }
    }

    @Test
    void testRateLimit_FirstRequestSucceeds() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        EmployeeRequest request = new EmployeeRequest("John", "Doe", organization.getId());
        String requestJson = objectMapper.writeValueAsString(request);

        // When - First request
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-RateLimit-Limit"))
                .andExpect(header().exists("X-RateLimit-Remaining"))
                .andExpect(header().string("X-RateLimit-Limit", "1"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"));
    }

    @Test
    void testRateLimit_SecondRequestExceedsLimit() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        EmployeeRequest request1 = new EmployeeRequest("John", "Doe", organization.getId());
        EmployeeRequest request2 = new EmployeeRequest("Jane", "Smith", organization.getId());
        String requestJson1 = objectMapper.writeValueAsString(request1);
        String requestJson2 = objectMapper.writeValueAsString(request2);

        // When - First request (should succeed)
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestJson1))
                .andExpect(status().isCreated());

        // Then - Second request (should be rate limited)
        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestJson2))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header().exists("X-RateLimit-Limit"))
                .andExpect(header().exists("X-RateLimit-Remaining"))
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string("X-RateLimit-Limit", "1"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andReturn();

        // Verify error response structure
        String responseBody = result.getResponse().getContentAsString();
        ApiError apiError = objectMapper.readValue(responseBody, ApiError.class);
        
        assertThat(apiError.getStatus()).isEqualTo(429);
        assertThat(apiError.getMessage()).contains("Rate limit exceeded");
        assertThat(apiError.getTimestamp()).isNotNull();
    }

    @Test
    void testRateLimit_OnlyWriteOperationsAreLimited() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        EmployeeRequest request = new EmployeeRequest("John", "Doe", organization.getId());
        String requestJson = objectMapper.writeValueAsString(request);

        // When - Exhaust the rate limit with a write operation
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestJson))
                .andExpect(status().isCreated());

        // Then - GET requests should not be rate limited
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());

        // And - Another GET request should still work
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    void testRateLimit_DifferentWriteMethodsAreLimited() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        EmployeeRequest createRequest = new EmployeeRequest("John", "Doe", organization.getId());
        String createJson = objectMapper.writeValueAsString(createRequest);

        // When - First request (POST) succeeds
        MvcResult createResult = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long employeeId = objectMapper.readTree(responseBody).get("id").asLong();

        // Then - Second write request (PUT) should be rate limited
        EmployeeRequest updateRequest = new EmployeeRequest("Jane", "Doe", organization.getId());
        String updateJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(updateJson))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testRateLimit_DeleteOperationIsLimited() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        EmployeeRequest request = new EmployeeRequest("John", "Doe", organization.getId());
        String requestJson = objectMapper.writeValueAsString(request);

        // When - First write request (POST) succeeds
        MvcResult createResult = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long employeeId = objectMapper.readTree(responseBody).get("id").asLong();

        // Then - Second write request (DELETE) should be rate limited
        mockMvc.perform(delete("/api/employees/{id}", employeeId))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }
}

