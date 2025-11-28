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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private Organization testOrganization;
    private Employee testEmployee;
    private EmployeeDto testEmployeeDto;
    private EmployeeRequest testEmployeeRequest;

    @BeforeEach
    void setUp() {
        testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);

        testEmployee = new Employee("John", "Doe", testOrganization);
        testEmployee.setId(1L);
        testEmployee.setDundieAwards(0);

        testEmployeeDto = new EmployeeDto(
                1L,
                "John",
                "Doe",
                1L,
                "Test Organization",
                0
        );

        testEmployeeRequest = new EmployeeRequest("John", "Doe", 1L);
    }

    @Test
    void testGetAllEmployees() {
        // Given
        Employee employee2 = new Employee("Jane", "Smith", testOrganization);
        employee2.setId(2L);
        List<Employee> employees = Arrays.asList(testEmployee, employee2);

        EmployeeDto employeeDto2 = new EmployeeDto(2L, "Jane", "Smith", 1L, "Test Organization", 0);
        List<EmployeeDto> expectedDtos = Arrays.asList(testEmployeeDto, employeeDto2);

        when(employeeRepository.findAll()).thenReturn(employees);
        when(employeeMapper.toDtoList(employees)).thenReturn(expectedDtos);

        // When
        List<EmployeeDto> result = employeeService.getAllEmployees();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedDtos);
        verify(employeeRepository).findAll();
        verify(employeeMapper).toDtoList(employees);
    }

    @Test
    void testGetAllEmployeesWhenEmpty() {
        // Given
        when(employeeRepository.findAll()).thenReturn(List.of());
        when(employeeMapper.toDtoList(any())).thenReturn(List.of());

        // When
        List<EmployeeDto> result = employeeService.getAllEmployees();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(employeeRepository).findAll();
        verify(employeeMapper).toDtoList(any());
    }

    @Test
    void testGetEmployeeById() {
        // Given
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toDto(testEmployee)).thenReturn(testEmployeeDto);

        // When
        EmployeeDto result = employeeService.getEmployee(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testEmployeeDto);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Doe");
        verify(employeeRepository).findById(1L);
        verify(employeeMapper).toDto(testEmployee);
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
        verify(employeeMapper, never()).toDto(any());
    }

    @Test
    void testCreateEmployee() {
        // Given
        Employee newEmployee = new Employee("John", "Doe", testOrganization);
        Employee savedEmployee = new Employee("John", "Doe", testOrganization);
        savedEmployee.setId(1L);
        savedEmployee.setDundieAwards(0);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));
        when(employeeMapper.fromCreateRequest(testEmployeeRequest)).thenReturn(newEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);
        when(employeeMapper.toDto(savedEmployee)).thenReturn(testEmployeeDto);

        // When
        EmployeeDto result = employeeService.createEmployee(testEmployeeRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testEmployeeDto);
        verify(organizationRepository).findById(1L);
        verify(employeeMapper).fromCreateRequest(testEmployeeRequest);
        verify(employeeRepository).save(any(Employee.class));
        verify(employeeMapper).toDto(savedEmployee);
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
        verify(employeeMapper, never()).fromCreateRequest(any());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void testUpdateEmployee() {
        // Given
        EmployeeRequest updateRequest = new EmployeeRequest("John", "Updated", 1L);
        Employee updatedEmployee = new Employee("John", "Updated", testOrganization);
        updatedEmployee.setId(1L);
        updatedEmployee.setDundieAwards(0);

        EmployeeDto updatedDto = new EmployeeDto(1L, "John", "Updated", 1L, "Test Organization", 0);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));
        doNothing().when(employeeMapper).updateEmployeeFromRequest(eq(updateRequest), any(Employee.class));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);
        when(employeeMapper.toDto(updatedEmployee)).thenReturn(updatedDto);

        // When
        EmployeeDto result = employeeService.updateEmployee(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.lastName()).isEqualTo("Updated");
        verify(employeeRepository).findById(1L);
        verify(organizationRepository).findById(1L);
        verify(employeeMapper).updateEmployeeFromRequest(eq(updateRequest), any(Employee.class));
        verify(employeeRepository).save(any(Employee.class));
        verify(employeeMapper).toDto(updatedEmployee);
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
        verify(organizationRepository, never()).findById(any());
        verify(employeeMapper, never()).updateEmployeeFromRequest(any(), any());
    }

    @Test
    void testUpdateEmployeeWithNonExistentOrganization() {
        // Given
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(organizationRepository.findById(999L)).thenReturn(Optional.empty());
        EmployeeRequest updateRequest = new EmployeeRequest("John", "Updated", 999L);

        // When/Then
        assertThatThrownBy(() -> employeeService.updateEmployee(1L, updateRequest))
                .isInstanceOf(OrganizationNotFoundException.class)
                .hasMessage("Organization with id 999 not found");

        verify(employeeRepository).findById(1L);
        verify(organizationRepository).findById(999L);
        verify(employeeMapper, never()).updateEmployeeFromRequest(any(), any());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void testDeleteEmployee() {
        // Given
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeRepository).delete(testEmployee);

        // When
        employeeService.deleteEmployee(1L);

        // Then
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).delete(testEmployee);
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
        verify(employeeRepository, never()).delete(any());
    }

    @Test
    void testAwardEmployee() {
        // Given
        testEmployee.setDundieAwards(0);
        Employee awardedEmployee = new Employee("John", "Doe", testOrganization);
        awardedEmployee.setId(1L);
        awardedEmployee.setDundieAwards(1);

        EmployeeDto awardedDto = new EmployeeDto(1L, "John", "Doe", 1L, "Test Organization", 1);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(awardedEmployee);
        when(employeeMapper.toDto(awardedEmployee)).thenReturn(awardedDto);

        // When
        EmployeeDto result = employeeService.awardEmployee(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.dundieAwards()).isEqualTo(1);
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(any(Employee.class));
        verify(employeeMapper).toDto(awardedEmployee);
    }

    @Test
    void testAwardEmployeeWithExistingAwards() {
        // Given
        testEmployee.setDundieAwards(5);
        Employee awardedEmployee = new Employee("John", "Doe", testOrganization);
        awardedEmployee.setId(1L);
        awardedEmployee.setDundieAwards(6);

        EmployeeDto awardedDto = new EmployeeDto(1L, "John", "Doe", 1L, "Test Organization", 6);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(awardedEmployee);
        when(employeeMapper.toDto(awardedEmployee)).thenReturn(awardedDto);

        // When
        EmployeeDto result = employeeService.awardEmployee(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.dundieAwards()).isEqualTo(6);
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(any(Employee.class));
        verify(employeeMapper).toDto(awardedEmployee);
    }

    @Test
    void testAwardEmployeeWithNullAwards() {
        // Given
        // Create employee with null dundieAwards (don't set it, leaving it null)
        Employee employeeWithNullAwards = new Employee("John", "Doe", testOrganization);
        employeeWithNullAwards.setId(1L);
        // dundieAwards is null by default, so we don't set it

        Employee awardedEmployee = new Employee("John", "Doe", testOrganization);
        awardedEmployee.setId(1L);
        awardedEmployee.setDundieAwards(1);

        EmployeeDto awardedDto = new EmployeeDto(1L, "John", "Doe", 1L, "Test Organization", 1);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employeeWithNullAwards));
        when(employeeRepository.save(any(Employee.class))).thenReturn(awardedEmployee);
        when(employeeMapper.toDto(awardedEmployee)).thenReturn(awardedDto);

        // When
        EmployeeDto result = employeeService.awardEmployee(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.dundieAwards()).isEqualTo(1);
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(any(Employee.class));
        verify(employeeMapper).toDto(awardedEmployee);
    }

    @Test
    void testAwardEmployeeNotFound() {
        // Given
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> employeeService.awardEmployee(999L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee with id 999 not found");

        verify(employeeRepository).findById(999L);
        verify(employeeRepository, never()).save(any());
        verify(employeeMapper, never()).toDto(any());
    }
}

