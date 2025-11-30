package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.service.OrganizationService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService service;

    public OrganizationController(OrganizationService service) {
        this.service = service;
    }
    
    @PostMapping("/{organizationId}/awards")
    @ResponseStatus(HttpStatus.OK)
    public List<EmployeeDto> awardAllInOrganization(
            @PathVariable Long organizationId,
            @RequestParam("type") String awardType
    ) {
        return service.awardAllEmployeesInOrganization(organizationId, awardType);
    }
}
