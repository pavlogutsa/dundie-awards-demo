package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.OrganizationDto;
import com.ninjaone.dundie_awards.dto.PageResponse;
import com.ninjaone.dundie_awards.model.AwardType;
import com.ninjaone.dundie_awards.service.OrganizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @GetMapping
    public PageResponse<OrganizationDto> getOrganizations(Pageable pageable) {
        Page<OrganizationDto> pageResult = service.getAllOrganizations(pageable);
        return PageResponse.from(pageResult);
    }
    
    @PostMapping("/{organizationId}/awards")
    @ResponseStatus(HttpStatus.OK)
    public List<EmployeeDto> awardAllInOrganization(
            @PathVariable Long organizationId,
            @RequestParam("type") AwardType awardType
    ) {
        return service.awardAllEmployeesInOrganization(organizationId, awardType);
    }
}
