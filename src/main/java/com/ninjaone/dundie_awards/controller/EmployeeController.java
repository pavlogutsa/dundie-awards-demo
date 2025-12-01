package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.AwardRequest;
import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.EmployeeRequest;
import com.ninjaone.dundie_awards.dto.PageResponse;
import com.ninjaone.dundie_awards.dto.UpdateEmployeeRequest;
import com.ninjaone.dundie_awards.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees", description = "API endpoints for managing employees and their awards")
@SecurityRequirement(name = "bearer-jwt")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @Operation(
            summary = "Get all employees",
            description = "Retrieves a paginated list of all employees"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employees retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @GetMapping
    public PageResponse<EmployeeDto> getAllEmployees(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        log.info("GET /api/employees - page={}, size={}, sort={}", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return PageResponse.from(service.getAllEmployees(pageable));
    }

    @Operation(
            summary = "Get employee by ID",
            description = "Retrieves a single employee by their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/{id}")
    public EmployeeDto get(
            @Parameter(description = "Employee ID", required = true)
            @PathVariable Long id) {
        log.info("GET /api/employees/{}", id);
        return service.getEmployee(id);
    }

    @Operation(
            summary = "Create a new employee",
            description = "Creates a new employee with the provided information"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Employee created successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request - validation errors"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDto create(
            @Parameter(description = "Employee creation request", required = true)
            @Valid @RequestBody EmployeeRequest req) {
        log.info("POST /api/employees - Creating employee: {} {}", req.firstName(), req.lastName());
        return service.createEmployee(req);
    }

    @Operation(
            summary = "Update an employee",
            description = "Updates an existing employee with the provided information (full update)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee updated successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request - validation errors"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PutMapping("/{id}")
    public EmployeeDto update(
            @Parameter(description = "Employee ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Employee update request", required = true)
            @Valid @RequestBody EmployeeRequest req) {
        log.info("PUT /api/employees/{} - Updating employee", id);
        return service.updateEmployee(id, req);
    }

    @Operation(
            summary = "Partially update an employee",
            description = "Partially updates an existing employee with the provided fields"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee updated successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PatchMapping("/{id}")
    public EmployeeDto patch(
            @Parameter(description = "Employee ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Employee partial update request", required = true)
            @RequestBody UpdateEmployeeRequest req) {
        log.info("PATCH /api/employees/{} - Partially updating employee", id);
        return service.patchEmployee(id, req);
    }

    @Operation(
            summary = "Delete an employee",
            description = "Deletes an employee by their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Employee ID", required = true)
            @PathVariable Long id) {
        log.info("DELETE /api/employees/{}", id);
        service.deleteEmployee(id);
    }

    @Operation(
            summary = "Award an employee",
            description = "Awards a Dundie award to an employee"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Award granted successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request - validation errors"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PostMapping("/{id}/awards")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeDto award(
            @Parameter(description = "Employee ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Award request", required = true)
            @Valid @RequestBody AwardRequest request) {
        log.info("POST /api/employees/{}/awards - Awarding employee", id);
        return service.awardEmployee(id, request);
    }

    @Operation(
            summary = "Remove award from an employee",
            description = "Removes the Dundie award from an employee"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Award removed successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @DeleteMapping("/{id}/awards")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeDto removeAward(
            @Parameter(description = "Employee ID", required = true)
            @PathVariable Long id) {
        log.info("DELETE /api/employees/{}/awards - Removing award from employee", id);
        return service.removeAward(id);
    }
}
