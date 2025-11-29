package com.ninjaone.dundie_awards.dto;

import com.ninjaone.dundie_awards.model.ActivityType;
import jakarta.validation.constraints.NotNull;

public record AwardRequest(
    @NotNull ActivityType activityType
) {}

