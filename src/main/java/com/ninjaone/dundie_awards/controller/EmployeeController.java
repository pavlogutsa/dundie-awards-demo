package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.AwardRequest;
import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.EmployeeRequest;
import com.ninjaone.dundie_awards.dto.PageResponse;
import com.ninjaone.dundie_awards.dto.UpdateEmployeeRequest;
import com.ninjaone.dundie_awards.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<EmployeeDto> getAllEmployees(Pageable pageable) {
        log.info("GET /api/employees - page={}, size={}, sort={}", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return PageResponse.from(service.getAllEmployees(pageable));
    }

    @GetMapping("/{id}")
    public EmployeeDto get(@PathVariable Long id) {
        log.info("GET /api/employees/{}", id);
        return service.getEmployee(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDto create(@Valid @RequestBody EmployeeRequest req) {
        log.info("POST /api/employees - Creating employee: {} {}", req.firstName(), req.lastName());
        return service.createEmployee(req);
    }

    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable Long id,
                              @Valid @RequestBody EmployeeRequest req) {
        log.info("PUT /api/employees/{} - Updating employee", id);
        return service.updateEmployee(id, req);
    }

    @PatchMapping("/{id}")
    public EmployeeDto patch(@PathVariable Long id,
                             @RequestBody UpdateEmployeeRequest req) {
        log.info("PATCH /api/employees/{} - Partially updating employee", id);
        return service.patchEmployee(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("DELETE /api/employees/{}", id);
        service.deleteEmployee(id);
    }

    @PostMapping("/{id}/awards")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeDto award(@PathVariable Long id, @Valid @RequestBody AwardRequest request) {
        log.info("POST /api/employees/{}/awards - Awarding employee", id);
        return service.awardEmployee(id, request);
    }

    @DeleteMapping("/{id}/awards")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeDto removeAward(@PathVariable Long id) {
        log.info("DELETE /api/employees/{}/awards - Removing award from employee", id);
        return service.removeAward(id);
    }
}
