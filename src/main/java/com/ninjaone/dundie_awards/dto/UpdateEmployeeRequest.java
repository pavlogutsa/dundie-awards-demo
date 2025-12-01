package com.ninjaone.dundie_awards.dto;

public record UpdateEmployeeRequest(
    String firstName,
    String lastName,
    Long organizationId
) {}

