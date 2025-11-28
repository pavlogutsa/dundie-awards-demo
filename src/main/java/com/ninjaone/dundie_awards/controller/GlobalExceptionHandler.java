package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.ApiError;
import com.ninjaone.dundie_awards.exception.ActivityNotFoundException;
import com.ninjaone.dundie_awards.exception.BusinessValidationException;
import com.ninjaone.dundie_awards.exception.EmployeeNotFoundException;
import com.ninjaone.dundie_awards.exception.OrganizationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String,String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        ApiError apiError = new ApiError(400, "Validation failed", errors);
        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessValidationException ex) {
        return ResponseEntity
                .badRequest()
                .body(new ApiError(400, ex.getMessage()));
    }

    @ExceptionHandler(OrganizationNotFoundException.class)
    public ResponseEntity<ApiError> handleOrganizationNotFound(OrganizationNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, ex.getMessage()));
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiError> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, ex.getMessage()));
    }

    @ExceptionHandler(ActivityNotFoundException.class)
    public ResponseEntity<ApiError> handleActivityNotFound(ActivityNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(500, "Unexpected error: " + ex.getMessage()));
    }
}
