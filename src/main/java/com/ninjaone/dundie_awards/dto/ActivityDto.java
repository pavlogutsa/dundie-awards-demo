package com.ninjaone.dundie_awards.dto;

import java.time.Instant;

public record ActivityDto(
    Long id,
    Instant occurredAt,
    Long employeeId,
    String event
) {}
