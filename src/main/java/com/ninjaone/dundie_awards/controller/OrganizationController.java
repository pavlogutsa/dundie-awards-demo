package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.OrganizationDto;
import com.ninjaone.dundie_awards.dto.PageResponse;
import com.ninjaone.dundie_awards.model.AwardType;
import com.ninjaone.dundie_awards.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/organizations")
@Tag(name = "Organizations", description = "API endpoints for managing organizations and awarding all employees")
@SecurityRequirement(name = "bearer-jwt")
public class OrganizationController {

    private final OrganizationService service;

    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

    @Operation(
            summary = "Get all organizations",
            description = "Retrieves a paginated list of all organizations"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Organizations retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @GetMapping
    public PageResponse<OrganizationDto> getOrganizations(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        log.info("GET /api/organizations - page={}, size={}, sort={}", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<OrganizationDto> pageResult = service.getAllOrganizations(pageable);
        return PageResponse.from(pageResult);
    }
    
    @Operation(
            summary = "Award all employees in an organization",
            description = "Awards all employees in the specified organization with the given award type"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Awards granted successfully to all employees",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request - invalid award type"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    @PostMapping("/{organizationId}/awards")
    @ResponseStatus(HttpStatus.OK)
    public List<EmployeeDto> awardAllInOrganization(
            @Parameter(description = "Organization ID", required = true)
            @PathVariable Long organizationId,
            @Parameter(description = "Award type to grant", required = true)
            @RequestParam("type") AwardType awardType
    ) {
        log.info("POST /api/organizations/{}/awards - Awarding all employees with type: {}", 
                organizationId, awardType);
        return service.awardAllEmployeesInOrganization(organizationId, awardType);
    }
}
