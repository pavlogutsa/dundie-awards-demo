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
import com.ninjaone.dundie_awards.model.AwardType;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.mockito.ArgumentMatcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    private EmployeeMapper employeeMapper;

    @Mock
    private ActivityRepository activityRepository;

    private EmployeeService employeeService;


    @BeforeEach
    void setUp() {
        employeeMapper = Mappers.getMapper(EmployeeMapper.class);
        employeeService = new EmployeeService(
                employeeRepository,
                organizationRepository,
                employeeMapper,
                activityRepository
        );
    }

    @Test
    void testGetAllEmployees() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();
        Employee testEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(0)
                .build();
        
        Employee employee2 = Employee.builder()
                .firstName("Jane")
                .lastName("Smith")
                .organization(testOrganization)
                .build();
        List<Employee> employees = Arrays.asList(testEmployee, employee2);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());

        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(employeePage);

        // When
        Page<EmployeeDto> result = employeeService.getAllEmployees(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        // Use real mapper to create expected DTOs for comparison
        List<EmployeeDto> expectedDtos = employeeMapper.toDtoList(employees);
        assertThat(result.getContent()).isEqualTo(expectedDtos);
        verify(employeeRepository).findAll(any(Pageable.class));
    }

    @Test
    void testGetAllEmployeesWhenEmpty() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Employee> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // When
        Page<EmployeeDto> result = employeeService.getAllEmployees(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
        verify(employeeRepository).findAll(any(Pageable.class));
    }

    @Test
    void testGetAllEmployeesWithPagination() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();
        List<Employee> employees = Arrays.asList(
                Employee.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .organization(testOrganization)
                        .dundieAwards(0)
                        .build(),
                Employee.builder()
                        .firstName("Jane")
                        .lastName("Smith")
                        .organization(testOrganization)
                        .dundieAwards(1)
                        .build()
        );
        
        Pageable pageable = PageRequest.of(0, 1);
        Page<Employee> firstPage = new PageImpl<>(employees.subList(0, 1), pageable, 2);

        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(firstPage);

        // When
        Page<EmployeeDto> result = employeeService.getAllEmployees(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isFalse();
        verify(employeeRepository).findAll(any(Pageable.class));
    }

    @Test
    void testGetEmployeeById() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();
        Employee testEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(0)
                .build();
        
        EmployeeDto testEmployeeDto = employeeMapper.toDto(testEmployee);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // When
        EmployeeDto result = employeeService.getEmployee(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testEmployeeDto);
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Doe");
        verify(employeeRepository).findById(1L);
    }

    @Test
    void testGetEmployeeByIdNotFound() {
        // Given
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> employeeService.getEmployee(999L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee with id 999 not found");

        verify(employeeRepository).findById(999L);
    }

    @Test
    void testCreateEmployee() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();
        
        EmployeeRequest testEmployeeRequest = new EmployeeRequest("John", "Doe", 1L);
        Employee savedEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(0)
                .build();
        EmployeeDto testEmployeeDto = employeeMapper.toDto(savedEmployee);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        // When
        EmployeeDto result = employeeService.createEmployee(testEmployeeRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testEmployeeDto);
        verify(organizationRepository).findById(1L);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testCreateEmployeeWithNonExistentOrganization() {
        // Given
        when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

        EmployeeRequest request = new EmployeeRequest("John", "Doe", 999L);

        // When/Then
        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(OrganizationNotFoundException.class)
                .hasMessage("Organization with id 999 not found");

        verify(organizationRepository).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();
        Employee testEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(0)
                .build();
        
        EmployeeRequest updateRequest = new EmployeeRequest("John", "Updated", 1L);
        Employee updatedEmployee = Employee.builder()
                .firstName("John")
                .lastName("Updated")
                .organization(testOrganization)
                .dundieAwards(0)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        // When
        EmployeeDto result = employeeService.updateEmployee(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.lastName()).isEqualTo("Updated");
        verify(employeeRepository).findById(1L);
        verify(organizationRepository).findById(1L);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployeeNotFound() {
        // Given
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        EmployeeRequest updateRequest = new EmployeeRequest("John", "Updated", 1L);

        // When/Then
        assertThatThrownBy(() -> employeeService.updateEmployee(999L, updateRequest))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee with id 999 not found");

        verify(employeeRepository).findById(999L);
        verify(organizationRepository, never()).findById(any(Long.class));
    }

    @Test
    void testUpdateEmployeeWithNonExistentOrganization() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();
        Employee testEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(0)
                .build();
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(organizationRepository.findById(999L)).thenReturn(Optional.empty());
        EmployeeRequest updateRequest = new EmployeeRequest("John", "Updated", 999L);

        // When/Then
        assertThatThrownBy(() -> employeeService.updateEmployee(1L, updateRequest))
                .isInstanceOf(OrganizationNotFoundException.class)
                .hasMessage("Organization with id 999 not found");

        verify(employeeRepository).findById(1L);
        verify(organizationRepository).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testDeleteEmployee() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();
        Employee testEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(0)
                .build();
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeRepository).delete(any(Employee.class));

        // When
        employeeService.deleteEmployee(1L);

        // Then
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).delete(any(Employee.class));
    }

    @Test
    void testDeleteEmployeeNotFound() {
        // Given
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> employeeService.deleteEmployee(999L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee with id 999 not found");

        verify(employeeRepository).findById(999L);
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    @Test
    void testAwardEmployee() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();
        Employee testEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(0)
                .build();
        
        Employee awardedEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(1)
                .build();

        AwardRequest awardRequest = new AwardRequest(AwardType.INNOVATION);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(awardedEmployee);

        // When
        EmployeeDto result = employeeService.awardEmployee(1L, awardRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.dundieAwards()).isEqualTo(1);
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(any(Employee.class));
        ArgumentMatcher<Activity> activityMatcher = new ArgumentMatcher<Activity>() {
            @Override
            public boolean matches(Activity activity) {
                Activity nonNullActivity = Objects.requireNonNull(activity);
                return nonNullActivity.getEvent() == ActivityType.AWARD_GRANTED &&
                       nonNullActivity.getEmployee() != null;
            }
        };
        verify(activityRepository).save(argThat(activityMatcher)); // NOSONAR - Mockito guarantees non-null
    }

    @Test
    void testAwardEmployeeWithExistingAwards() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();
        Employee testEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(5)
                .build();
        
        Employee awardedEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(6)
                .build();

        AwardRequest awardRequest = new AwardRequest(AwardType.INNOVATION);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(awardedEmployee);

        // When
        EmployeeDto result = employeeService.awardEmployee(1L, awardRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.dundieAwards()).isEqualTo(6);
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(any(Employee.class));
        ArgumentMatcher<Activity> activityMatcher = new ArgumentMatcher<Activity>() {
            @Override
            public boolean matches(Activity activity) {
                Activity nonNullActivity = Objects.requireNonNull(activity);
                return nonNullActivity.getEvent() == ActivityType.AWARD_GRANTED &&
                       nonNullActivity.getEmployee() != null;
            }
        };
        verify(activityRepository).save(argThat(activityMatcher)); // NOSONAR - Mockito guarantees non-null
    }

    @Test
    void testAwardEmployeeWithNullAwards() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();
        
        // Create employee with null dundieAwards (don't set it, leaving it null)
        Employee employeeWithNullAwards = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .build();
        // dundieAwards is null by default, so we don't set it

        Employee awardedEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(1)
                .build();

        AwardRequest awardRequest = new AwardRequest(AwardType.INNOVATION);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employeeWithNullAwards));
        when(employeeRepository.save(any(Employee.class))).thenReturn(awardedEmployee);

        // When
        EmployeeDto result = employeeService.awardEmployee(1L, awardRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.dundieAwards()).isEqualTo(1);
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(any(Employee.class));
        ArgumentMatcher<Activity> activityMatcher = new ArgumentMatcher<Activity>() {
            @Override
            public boolean matches(Activity activity) {
                Activity nonNullActivity = Objects.requireNonNull(activity);
                return nonNullActivity.getEvent() == ActivityType.AWARD_GRANTED &&
                       nonNullActivity.getEmployee() != null;
            }
        };
        verify(activityRepository).save(argThat(activityMatcher)); // NOSONAR - Mockito guarantees non-null
    }

    @Test
    void testAwardEmployeeNotFound() {
        // Given
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        AwardRequest awardRequest = new AwardRequest(AwardType.INNOVATION);

        // When/Then
        assertThatThrownBy(() -> employeeService.awardEmployee(999L, awardRequest))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee with id 999 not found");

        verify(employeeRepository).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void testRemoveAward() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();
        Employee testEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(2)
                .build();
        
        Employee employeeAfterRemoval = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(1)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employeeAfterRemoval);

        // When
        EmployeeDto result = employeeService.removeAward(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.dundieAwards()).isEqualTo(1);
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(any(Employee.class));
        ArgumentMatcher<Activity> activityMatcher = activity -> {
            Activity nonNullActivity = Objects.requireNonNull(activity);
            return nonNullActivity.getEvent() == ActivityType.AWARD_REMOVED &&
                   nonNullActivity.getEmployee() == employeeAfterRemoval;
        };
        verify(activityRepository).save(argThat(activityMatcher)); // NOSONAR - Mockito guarantees non-null
    }

    @Test
    void testRemoveAwardWithNoAwards() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();
        Employee testEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(0)
                .build();
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // When/Then
        assertThatThrownBy(() -> employeeService.removeAward(1L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Employee has no awards to remove");

        verify(employeeRepository).findById(1L);
        verify(employeeRepository, never()).save(any(Employee.class));
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void testRemoveAwardWithNullAwards() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();
        Employee testEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .build();
        // dundieAwards is null by default
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // When/Then
        assertThatThrownBy(() -> employeeService.removeAward(1L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Employee has no awards to remove");

        verify(employeeRepository).findById(1L);
        verify(employeeRepository, never()).save(any(Employee.class));
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void testRemoveAwardNotFound() {
        // Given
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> employeeService.removeAward(999L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee with id 999 not found");

        verify(employeeRepository).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
        verify(activityRepository, never()).save(any(Activity.class));
    }
}

