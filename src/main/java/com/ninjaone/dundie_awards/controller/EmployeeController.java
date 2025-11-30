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

    @DeleteMapping("/{id}/awards")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeDto removeAward(@PathVariable Long id) {
        return service.removeAward(id);
    }
}
