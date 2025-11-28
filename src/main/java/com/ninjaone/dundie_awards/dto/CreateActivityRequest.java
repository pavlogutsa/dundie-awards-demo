package com.ninjaone.dundie_awards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateActivityRequest(
    @NotNull Long employeeId,
    @NotNull Instant occurredAt,
    @NotBlank String event
) {}
