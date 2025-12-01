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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

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
        return employeeRepository.findAll(pageable)
                .map(employeeMapper::toDto);
    }

    @Transactional(readOnly=true)
    public EmployeeDto getEmployee(Long id) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return employeeMapper.toDto(e);
    }

    public EmployeeDto createEmployee(EmployeeRequest req) {
        Long organizationId = Objects.requireNonNull(req.organizationId());
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));

        Employee e = employeeMapper.fromCreateRequest(req);
        e.setOrganization(org);

        return employeeMapper.toDto(employeeRepository.save(e));
    }

    public EmployeeDto updateEmployee(Long id, EmployeeRequest req) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        Long organizationId = Objects.requireNonNull(req.organizationId());
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));

        employeeMapper.updateEmployeeFromRequest(req, e);
        e.setOrganization(org);

        return employeeMapper.toDto(employeeRepository.save(e));
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
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        employeeRepository.delete(e);
    }

    @Transactional
    public EmployeeDto awardEmployee(Long id, AwardRequest request) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

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

        return employeeMapper.toDto(saved);
    }

    @Transactional
    public EmployeeDto removeAward(Long id) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        Integer current = e.getDundieAwards();
        if (current == null || current <= 0) {
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

        return employeeMapper.toDto(saved);
    }
}
