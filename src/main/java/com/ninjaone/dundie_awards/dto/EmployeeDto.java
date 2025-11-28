package com.ninjaone.dundie_awards.dto;

public record EmployeeDto(
    Long id,
    String firstName,
    String lastName,
    Long organizationId,
    String organizationName,
    Integer dundieAwards
) {}
