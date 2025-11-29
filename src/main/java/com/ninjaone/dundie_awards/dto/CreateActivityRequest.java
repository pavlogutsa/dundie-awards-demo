package com.ninjaone.dundie_awards.dto;

import com.ninjaone.dundie_awards.model.ActivityType;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateActivityRequest(
    @NotNull Long employeeId,
    @NotNull Instant occurredAt,
    @NotNull ActivityType event
) {}
