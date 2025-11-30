package com.ninjaone.dundie_awards.dto;

import com.ninjaone.dundie_awards.model.AwardType;
import jakarta.validation.constraints.NotNull;

public record AwardRequest(
    @NotNull AwardType awardType
) {}

