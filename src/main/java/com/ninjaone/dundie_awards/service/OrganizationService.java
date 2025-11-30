package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.exception.BusinessValidationException;
import com.ninjaone.dundie_awards.exception.OrganizationNotFoundException;
import com.ninjaone.dundie_awards.mapper.EmployeeMapper;
import com.ninjaone.dundie_awards.model.Award;
import com.ninjaone.dundie_awards.model.AwardType;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public OrganizationService(EmployeeRepository employeeRepository,
                           OrganizationRepository organizationRepository,
                            EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.organizationRepository = organizationRepository;
        this.employeeMapper = employeeMapper;
    }

    @Transactional
    public List<EmployeeDto> awardAllEmployeesInOrganization(Long organizationId, String awardTypeValue) {
        Long orgId = Objects.requireNonNull(organizationId);
        organizationRepository.findById(orgId)
                .orElseThrow(() -> new OrganizationNotFoundException(orgId));

        AwardType awardType = Arrays.stream(AwardType.values())
                .filter(t -> t.name().equalsIgnoreCase(awardTypeValue))
                .findFirst()
                .orElseThrow(() ->
                        new BusinessValidationException("Unknown award type: " + awardTypeValue));

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
