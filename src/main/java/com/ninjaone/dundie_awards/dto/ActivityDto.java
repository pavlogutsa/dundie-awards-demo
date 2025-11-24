package com.ninjaone.dundie_awards.dto;

import java.time.LocalDateTime;

public class ActivityDto {
    private Long id;
    private LocalDateTime occurredAt;
    private Long employeeId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
}
