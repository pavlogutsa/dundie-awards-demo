package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.OrganizationDto;
import com.ninjaone.dundie_awards.exception.BusinessValidationException;
import com.ninjaone.dundie_awards.exception.OrganizationNotFoundException;
import com.ninjaone.dundie_awards.mapper.EmployeeMapper;
import com.ninjaone.dundie_awards.mapper.OrganizationMapper;
import com.ninjaone.dundie_awards.model.Award;
import com.ninjaone.dundie_awards.model.AwardType;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.NonNull;

import java.time.Instant;
import java.util.List;
import java.util.Arrays;
import java.util.Objects;

@Service
@Transactional
public class OrganizationService {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeMapper employeeMapper;
    private final OrganizationMapper organizationMapper;

    public OrganizationService(EmployeeRepository employeeRepository,
                           OrganizationRepository organizationRepository,
                            EmployeeMapper employeeMapper,
                            OrganizationMapper organizationMapper) {
        this.employeeRepository = employeeRepository;
        this.organizationRepository = organizationRepository;
        this.employeeMapper = employeeMapper;
        this.organizationMapper = organizationMapper;
    }

    @Transactional(readOnly=true)
    public Page<OrganizationDto> getAllOrganizations(@NonNull Pageable pageable) {
        return organizationRepository.findAll(pageable)
                .map(organizationMapper::toDto);
    }

    @Transactional
    public List<EmployeeDto> awardAllEmployeesInOrganization(Long organizationId, AwardType awardType) {
        Long orgId = Objects.requireNonNull(organizationId);
        organizationRepository.findById(orgId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));

        List<Employee> employees = employeeRepository.findByOrganizationId(orgId);

        if (employees.isEmpty()) {
            throw new BusinessValidationException(
                    "Organization " + organizationId + " has no employees to award");
        }

        Instant now = Instant.now();
        for (Employee e : employees) {
            Award award = Award.builder()
                    .type(awardType)
                    .awardedAt(now)
                    .employee(e)
                    .build();
            e.addAward(award);
        }

        List<Employee> saved = employeeRepository.saveAll(employees);
        return employeeMapper.toDtoList(saved);
    }
}
