package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.ApiError;
import com.ninjaone.dundie_awards.dto.AwardRequest;
import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.EmployeeRequest;
import com.ninjaone.dundie_awards.exception.EmployeeNotFoundException;
import com.ninjaone.dundie_awards.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public List<EmployeeDto> getEmployees() {
        return service.getAllEmployees();
    }

    @GetMapping("/{id}")
    public EmployeeDto get(@PathVariable Long id) {
        return service.getEmployee(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDto create(@Valid @RequestBody EmployeeRequest req) {
        return service.createEmployee(req);
    }

    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable Long id,
                              @Valid @RequestBody EmployeeRequest req) {
        return service.updateEmployee(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteEmployee(id);
    }

    @PostMapping("/{id}/awards")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeDto award(@PathVariable Long id, @Valid @RequestBody AwardRequest request) {
        return service.awardEmployee(id, request);
    }

    /**
     * Award all employees belonging to the given organization.
     *
     * Example: POST /api/employees/organization/5/awards
     */
    @PostMapping("/organization/{organizationId}/awards")
    @ResponseStatus(HttpStatus.OK)
    public List<EmployeeDto> awardAllInOrganization(@PathVariable Long organizationId) {
        return service.awardAllEmployeesInOrganization(organizationId);
    }
    
    @DeleteMapping("/{id}/awards")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeDto removeAward(@PathVariable Long id) {
        return service.removeAward(id);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EmployeeNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, ex.getMessage()));
    }
}
