package com.ninjaone.dundie_awards.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CreateActivityRequest {

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDateTime occurredAt;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
