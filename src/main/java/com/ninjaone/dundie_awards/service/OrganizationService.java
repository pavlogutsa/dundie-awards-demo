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
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
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
        log.debug("Getting all organizations with pagination: page={}, size={}, sort={}", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<OrganizationDto> result = organizationRepository.findAll(pageable)
                .map(organizationMapper::toDto);
        log.debug("Retrieved {} organizations (total: {})", result.getNumberOfElements(), result.getTotalElements());
        return result;
    }

    @Transactional
    public List<EmployeeDto> awardAllEmployeesInOrganization(Long organizationId, AwardType awardType) {
        log.info("Awarding all employees in organization {} with award type: {}", organizationId, awardType);
        try {
            Long orgId = Objects.requireNonNull(organizationId);
            organizationRepository.findById(orgId)
                    .orElseThrow(() -> {
                        log.warn("Organization not found with id: {}", organizationId);
                        return new OrganizationNotFoundException(organizationId);
                    });

            List<Employee> employees = employeeRepository.findByOrganizationId(orgId);

            if (employees.isEmpty()) {
                log.warn("Organization {} has no employees to award", organizationId);
                throw new BusinessValidationException(
                        "Organization " + organizationId + " has no employees to award");
            }

            log.debug("Awarding {} employees in organization {}", employees.size(), organizationId);
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
            log.info("Successfully awarded {} employees in organization {}", saved.size(), organizationId);
            return employeeMapper.toDtoList(saved);
        } catch (Exception e) {
            log.error("Failed to award employees in organization {}", organizationId, e);
            throw e;
        }
    }
}
