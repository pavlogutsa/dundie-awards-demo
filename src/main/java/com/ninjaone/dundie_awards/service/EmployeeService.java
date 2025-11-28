package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.EmployeeRequest;
import com.ninjaone.dundie_awards.exception.EmployeeNotFoundException;
import com.ninjaone.dundie_awards.exception.OrganizationNotFoundException;
import com.ninjaone.dundie_awards.mapper.EmployeeMapper;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeService(EmployeeRepository employeeRepository,
                           OrganizationRepository organizationRepository,
                           EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.organizationRepository = organizationRepository;
        this.employeeMapper = employeeMapper;
    }

    @Transactional(readOnly=true)
    public List<EmployeeDto> getAllEmployees() {
        return employeeMapper.toDtoList(employeeRepository.findAll());
    }

    @Transactional(readOnly=true)
    public EmployeeDto getEmployee(Long id) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return employeeMapper.toDto(e);
    }

    public EmployeeDto createEmployee(EmployeeRequest req) {
        Organization org = organizationRepository.findById(req.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(req.organizationId()));

        Employee e = employeeMapper.fromCreateRequest(req);
        e.setOrganization(org);

        return employeeMapper.toDto(employeeRepository.save(e));
    }

    public EmployeeDto updateEmployee(Long id, EmployeeRequest req) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        Organization org = organizationRepository.findById(req.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(req.organizationId()));

        employeeMapper.updateEmployeeFromRequest(req, e);
        e.setOrganization(org);

        return employeeMapper.toDto(employeeRepository.save(e));
    }

    public void deleteEmployee(Long id) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        employeeRepository.delete(e);
    }

    @Transactional
    public EmployeeDto awardEmployee(Long id) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        Integer current = e.getDundieAwards();
        if (current == null) {
            current = 0;
        }
        e.setDundieAwards(current + 1);

        Employee saved = employeeRepository.save(e);
        return employeeMapper.toDto(saved);
    }
}
