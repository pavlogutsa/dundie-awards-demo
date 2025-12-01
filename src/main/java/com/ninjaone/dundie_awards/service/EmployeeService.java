package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.AwardRequest;
import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.EmployeeRequest;
import com.ninjaone.dundie_awards.exception.BusinessValidationException;
import com.ninjaone.dundie_awards.exception.EmployeeNotFoundException;
import com.ninjaone.dundie_awards.exception.OrganizationNotFoundException;
import com.ninjaone.dundie_awards.mapper.EmployeeMapper;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.model.ActivityType;
import com.ninjaone.dundie_awards.model.Award;
import com.ninjaone.dundie_awards.model.AwardType;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeMapper employeeMapper;
    private final ActivityRepository activityRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           OrganizationRepository organizationRepository,
                           EmployeeMapper employeeMapper,
                           ActivityRepository activityRepository) {
        this.employeeRepository = employeeRepository;
        this.organizationRepository = organizationRepository;
        this.employeeMapper = employeeMapper;
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly=true)
    public Page<EmployeeDto> getAllEmployees(@NonNull Pageable pageable) {
        log.debug("Getting all employees with pagination: page={}, size={}, sort={}", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<EmployeeDto> result = employeeRepository.findAll(pageable)
                .map(employeeMapper::toDto);
        log.debug("Retrieved {} employees (total: {})", result.getNumberOfElements(), result.getTotalElements());
        return result;
    }

    @Transactional(readOnly=true)
    public EmployeeDto getEmployee(Long id) {
        log.debug("Getting employee with id: {}", id);
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Employee not found with id: {}", id);
                    return new EmployeeNotFoundException(id);
                });
        log.debug("Found employee: {} {} (id: {})", e.getFirstName(), e.getLastName(), e.getId());
        return employeeMapper.toDto(e);
    }

    public EmployeeDto createEmployee(EmployeeRequest req) {
        log.info("Creating employee: {} {} for organization {}", 
                req.firstName(), req.lastName(), req.organizationId());
        try {
            Long organizationId = Objects.requireNonNull(req.organizationId());
            Organization org = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> {
                        log.warn("Organization not found with id: {}", organizationId);
                        return new OrganizationNotFoundException(organizationId);
                    });

            Employee e = employeeMapper.fromCreateRequest(req);
            e.setOrganization(org);

            EmployeeDto saved = employeeMapper.toDto(employeeRepository.save(e));
            log.info("Successfully created employee: {} {} (id: {})", 
                    saved.firstName(), saved.lastName(), saved.id());
            return saved;
        } catch (Exception e) {
            log.error("Failed to create employee: {} {}", req.firstName(), req.lastName(), e);
            throw e;
        }
    }

    public EmployeeDto updateEmployee(Long id, EmployeeRequest req) {
        log.info("Updating employee with id: {} to {} {}", id, req.firstName(), req.lastName());
        try {
            Employee e = employeeRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Employee not found with id: {}", id);
                        return new EmployeeNotFoundException(id);
                    });

            Long organizationId = Objects.requireNonNull(req.organizationId());
            Organization org = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> {
                        log.warn("Organization not found with id: {}", organizationId);
                        return new OrganizationNotFoundException(organizationId);
                    });

            employeeMapper.updateEmployeeFromRequest(req, e);
            e.setOrganization(org);

            EmployeeDto updated = employeeMapper.toDto(employeeRepository.save(e));
            log.info("Successfully updated employee (id: {})", updated.id());
            return updated;
        } catch (Exception e) {
            log.error("Failed to update employee with id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public List<EmployeeDto> awardAllEmployeesInOrganization(Long organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));

        List<Employee> employees = employeeRepository.findByOrganizationId(organizationId);
        if (employees.isEmpty()) {
            throw new BusinessValidationException(
                    "Organization " + organizationId + " has no employees to award");
        }

        Instant now = Instant.now();

        for (Employee e : employees) {
            // create a new Award for this employee
            Award award = Award.builder()
                    .type(AwardType.INNOVATION)
                    .awardedAt(now)
                    .employee(e)
                    .build();

            // maintain bidirectional relationship and counter
            e.addAward(award);

            // Option A: rely on cascade from Employee to Award (with cascade = ALL)
            // Option B: explicitly save awards via awardRepository
            // Here, with cascade, just saving employees is enough.
        }

        List<Employee> saved = employeeRepository.saveAll(employees);
        return employeeMapper.toDtoList(saved);
    }

    public void deleteEmployee(Long id) {
        log.info("Deleting employee with id: {}", id);
        try {
            Employee e = employeeRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Employee not found with id: {}", id);
                        return new EmployeeNotFoundException(id);
                    });
            employeeRepository.delete(e);
            log.info("Successfully deleted employee (id: {})", id);
        } catch (Exception e) {
            log.error("Failed to delete employee with id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public EmployeeDto awardEmployee(Long id, AwardRequest request) {
        log.info("Awarding employee with id: {} (award type: {})", id, request.awardType());
        try {
            Employee e = employeeRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Employee not found with id: {}", id);
                        return new EmployeeNotFoundException(id);
                    });

            Integer current = e.getDundieAwards();
            if (current == null) {
                current = 0;
            }
            e.setDundieAwards(current + 1);

            Employee saved = employeeRepository.save(e);

            // Create activity for the award with the specified activity type
            Activity activity = new Activity();
            activity.setEmployee(saved);
            activity.setOccurredAt(Instant.now());
            activity.setEvent(ActivityType.AWARD_GRANTED);
            activityRepository.save(activity);

            log.info("Successfully awarded employee (id: {}), new award count: {}", 
                    saved.getId(), saved.getDundieAwards());
            return employeeMapper.toDto(saved);
        } catch (Exception e) {
            log.error("Failed to award employee with id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public EmployeeDto removeAward(Long id) {
        log.info("Removing award from employee with id: {}", id);
        try {
            Employee e = employeeRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Employee not found with id: {}", id);
                        return new EmployeeNotFoundException(id);
                    });

            Integer current = e.getDundieAwards();
            if (current == null || current <= 0) {
                log.warn("Attempted to remove award from employee (id: {}) with no awards (current: {})", 
                        id, current);
                throw new BusinessValidationException("Employee has no awards to remove");
            }
            e.setDundieAwards(current - 1);

            Employee saved = employeeRepository.save(e);

            // Create activity for the award removal
            Activity activity = new Activity();
            activity.setEmployee(saved);
            activity.setOccurredAt(Instant.now());
            activity.setEvent(ActivityType.AWARD_REMOVED);
            activityRepository.save(activity);

            log.info("Successfully removed award from employee (id: {}), new award count: {}", 
                    saved.getId(), saved.getDundieAwards());
            return employeeMapper.toDto(saved);
        } catch (Exception e) {
            log.error("Failed to remove award from employee with id: {}", id, e);
            throw e;
        }
    }
}
