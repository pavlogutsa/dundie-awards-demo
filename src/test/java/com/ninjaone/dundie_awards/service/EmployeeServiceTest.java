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
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        // Use real MapStruct mapper instead of mock
        employeeMapper = new com.ninjaone.dundie_awards.mapper.EmployeeMapperImpl();
        // Manually inject the real mapper into the service since @InjectMocks won't work with manual initialization
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
        Organization testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);
        Employee testEmployee = new Employee("John", "Doe", testOrganization);
        testEmployee.setId(1L);
        testEmployee.setDundieAwards(0);
        
        Employee employee2 = new Employee("Jane", "Smith", testOrganization);
        employee2.setId(2L);
        List<Employee> employees = Arrays.asList(testEmployee, employee2);

        when(employeeRepository.findAll()).thenReturn(employees);

        // When
        List<EmployeeDto> result = employeeService.getAllEmployees();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        // Use real mapper to create expected DTOs for comparison
        List<EmployeeDto> expectedDtos = employeeMapper.toDtoList(employees);
        assertThat(result).isEqualTo(expectedDtos);
        verify(employeeRepository).findAll();
    }

    @Test
    void testGetAllEmployeesWhenEmpty() {
        // Given
        when(employeeRepository.findAll()).thenReturn(List.of());

        // When
        List<EmployeeDto> result = employeeService.getAllEmployees();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(employeeRepository).findAll();
    }

    @Test
    void testGetEmployeeById() {
        // Given
        Organization testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);
        Employee testEmployee = new Employee("John", "Doe", testOrganization);
        testEmployee.setId(1L);
        testEmployee.setDundieAwards(0);
        
        EmployeeDto testEmployeeDto = employeeMapper.toDto(testEmployee);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // When
        EmployeeDto result = employeeService.getEmployee(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testEmployeeDto);
        assertThat(result.id()).isEqualTo(1L);
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
        Organization testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);
        
        EmployeeRequest testEmployeeRequest = new EmployeeRequest("John", "Doe", 1L);
        Employee savedEmployee = new Employee("John", "Doe", testOrganization);
        savedEmployee.setId(1L);
        savedEmployee.setDundieAwards(0);
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
        Organization testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);
        Employee testEmployee = new Employee("John", "Doe", testOrganization);
        testEmployee.setId(1L);
        testEmployee.setDundieAwards(0);
        
        EmployeeRequest updateRequest = new EmployeeRequest("John", "Updated", 1L);
        Employee updatedEmployee = new Employee("John", "Updated", testOrganization);
        updatedEmployee.setId(1L);
        updatedEmployee.setDundieAwards(0);

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
        Organization testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);
        Employee testEmployee = new Employee("John", "Doe", testOrganization);
        testEmployee.setId(1L);
        testEmployee.setDundieAwards(0);
        
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
        Organization testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);
        Employee testEmployee = new Employee("John", "Doe", testOrganization);
        testEmployee.setId(1L);
        testEmployee.setDundieAwards(0);
        
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
        Organization testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);
        Employee testEmployee = new Employee("John", "Doe", testOrganization);
        testEmployee.setId(1L);
        testEmployee.setDundieAwards(0);
        
        Employee awardedEmployee = new Employee("John", "Doe", testOrganization);
        awardedEmployee.setId(1L);
        awardedEmployee.setDundieAwards(1);

        AwardRequest awardRequest = new AwardRequest(ActivityType.HELPED_TEAMMATE);

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
                return nonNullActivity.getEvent() == ActivityType.HELPED_TEAMMATE &&
                       nonNullActivity.getEmployee().getId() == 1L;
            }
        };
        verify(activityRepository).save(argThat(activityMatcher)); // NOSONAR - Mockito guarantees non-null
    }

    @Test
    void testAwardEmployeeWithExistingAwards() {
        // Given
        Organization testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);
        Employee testEmployee = new Employee("John", "Doe", testOrganization);
        testEmployee.setId(1L);
        testEmployee.setDundieAwards(5);
        
        Employee awardedEmployee = new Employee("John", "Doe", testOrganization);
        awardedEmployee.setId(1L);
        awardedEmployee.setDundieAwards(6);

        AwardRequest awardRequest = new AwardRequest(ActivityType.COMPLETED_PROJECT);

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
                return nonNullActivity.getEvent() == ActivityType.COMPLETED_PROJECT &&
                       nonNullActivity.getEmployee().getId() == 1L;
            }
        };
        verify(activityRepository).save(argThat(activityMatcher)); // NOSONAR - Mockito guarantees non-null
    }

    @Test
    void testAwardEmployeeWithNullAwards() {
        // Given
        Organization testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);
        
        // Create employee with null dundieAwards (don't set it, leaving it null)
        Employee employeeWithNullAwards = new Employee("John", "Doe", testOrganization);
        employeeWithNullAwards.setId(1L);
        // dundieAwards is null by default, so we don't set it

        Employee awardedEmployee = new Employee("John", "Doe", testOrganization);
        awardedEmployee.setId(1L);
        awardedEmployee.setDundieAwards(1);

        AwardRequest awardRequest = new AwardRequest(ActivityType.MENTORED_COLLEAGUE);

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
                return nonNullActivity.getEvent() == ActivityType.MENTORED_COLLEAGUE &&
                       nonNullActivity.getEmployee().getId() == 1L;
            }
        };
        verify(activityRepository).save(argThat(activityMatcher)); // NOSONAR - Mockito guarantees non-null
    }

    @Test
    void testAwardEmployeeNotFound() {
        // Given
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        AwardRequest awardRequest = new AwardRequest(ActivityType.INNOVATION);

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
        Organization testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);
        Employee testEmployee = new Employee("John", "Doe", testOrganization);
        testEmployee.setId(1L);
        testEmployee.setDundieAwards(2);
        
        Employee employeeAfterRemoval = new Employee("John", "Doe", testOrganization);
        employeeAfterRemoval.setId(1L);
        employeeAfterRemoval.setDundieAwards(1);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employeeAfterRemoval);

        // When
        EmployeeDto result = employeeService.removeAward(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.dundieAwards()).isEqualTo(1);
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(any(Employee.class));
        ArgumentMatcher<Activity> activityMatcher = new ArgumentMatcher<Activity>() {
            @Override
            public boolean matches(Activity activity) {
                Activity nonNullActivity = Objects.requireNonNull(activity);
                return nonNullActivity.getEvent() == ActivityType.AWARD_REMOVED &&
                       nonNullActivity.getEmployee().getId() == 1L;
            }
        };
        verify(activityRepository).save(argThat(activityMatcher)); // NOSONAR - Mockito guarantees non-null
    }

    @Test
    void testRemoveAwardWithNoAwards() {
        // Given
        Organization testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);
        Employee testEmployee = new Employee("John", "Doe", testOrganization);
        testEmployee.setId(1L);
        testEmployee.setDundieAwards(0);
        
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
        Organization testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);
        Employee testEmployee = new Employee("John", "Doe", testOrganization);
        testEmployee.setId(1L);
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

