package com.ninjaone.dundie_awards.dto;

import java.time.Instant;
import java.util.Map;

public class ApiError {
    private Instant timestamp = Instant.now();
    private int status;
    private String message;
    private Map<String,String> validationErrors;

    public ApiError() {}
    public ApiError(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public ApiError(int status, String message, Map<String,String> validationErrors) {
        this.status = status;
        this.message = message;
        this.validationErrors = validationErrors;
    }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String,String> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(Map<String,String> validationErrors) { this.validationErrors = validationErrors; }
}
