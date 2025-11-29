package com.ninjaone.dundie_awards.dto;

import com.ninjaone.dundie_awards.model.ActivityType;
import java.time.Instant;

public record ActivityDto(
    Long id,
    Instant occurredAt,
    Long employeeId,
    ActivityType event
) {}
