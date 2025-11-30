package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.exception.BusinessValidationException;
import com.ninjaone.dundie_awards.exception.OrganizationNotFoundException;
import com.ninjaone.dundie_awards.mapper.EmployeeMapper;
import com.ninjaone.dundie_awards.model.AwardType;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class OrganizationServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    private EmployeeMapper employeeMapper;

    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        employeeMapper = Mappers.getMapper(EmployeeMapper.class);
        organizationService = new OrganizationService(
                employeeRepository,
                organizationRepository,
                employeeMapper
        );
    }

    @Test
    void testAwardAllEmployeesInOrganization_ValidCase_TwoEmployees() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();

        Employee employee1 = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(0)
                .build();

        Employee employee2 = Employee.builder()
                .firstName("Jane")
                .lastName("Smith")
                .organization(testOrganization)
                .dundieAwards(1)
                .build();

        List<Employee> employees = Arrays.asList(employee1, employee2);

        // Employees after award (dundieAwards incremented)
        Employee employee1AfterAward = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(1)
                .build();

        Employee employee2AfterAward = Employee.builder()
                .firstName("Jane")
                .lastName("Smith")
                .organization(testOrganization)
                .dundieAwards(2)
                .build();

        List<Employee> savedEmployees = Arrays.asList(employee1AfterAward, employee2AfterAward);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));
        when(employeeRepository.findByOrganizationId(1L)).thenReturn(employees);
        when(employeeRepository.saveAll(anyList())).thenReturn(savedEmployees);

        // When
        List<EmployeeDto> result = organizationService.awardAllEmployeesInOrganization(1L, "INNOVATION");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        
        // Verify organization was looked up
        verify(organizationRepository).findById(1L);
        
        // Verify employees were retrieved
        verify(employeeRepository).findByOrganizationId(1L);
        
        // Verify employees were saved (with awards added)
        verify(employeeRepository).saveAll(anyList());
        
        // Verify awards were added to employees (dundieAwards incremented)
        assertThat(employee1.getDundieAwards()).isEqualTo(1);
        assertThat(employee2.getDundieAwards()).isEqualTo(2);
        
        // Verify awards were created and added
        assertThat(employee1.getAwards()).hasSize(1);
        assertThat(employee2.getAwards()).hasSize(1);
        assertThat(employee1.getAwards().get(0).getType()).isEqualTo(AwardType.INNOVATION);
        assertThat(employee2.getAwards().get(0).getType()).isEqualTo(AwardType.INNOVATION);
    }

    @Test
    void testAwardAllEmployeesInOrganization_NoEmployees() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));
        when(employeeRepository.findByOrganizationId(1L)).thenReturn(List.of());

        // When/Then
        assertThatThrownBy(() -> organizationService.awardAllEmployeesInOrganization(1L, "INNOVATION"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Organization 1 has no employees to award");

        verify(organizationRepository).findById(1L);
        verify(employeeRepository).findByOrganizationId(1L);
        verify(employeeRepository, never()).saveAll(anyList());
    }

    @Test
    void testAwardAllEmployeesInOrganization_OrganizationNotFound() {
        // Given
        when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> organizationService.awardAllEmployeesInOrganization(999L, "INNOVATION"))
                .isInstanceOf(OrganizationNotFoundException.class)
                .hasMessage("Organization with id 999 not found");

        verify(organizationRepository).findById(999L);
        verify(employeeRepository, never()).findByOrganizationId(any(Long.class));
        verify(employeeRepository, never()).saveAll(anyList());
    }

    @Test
    void testAwardAllEmployeesInOrganization_AwardTypeNotFound() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));

        // When/Then
        assertThatThrownBy(() -> organizationService.awardAllEmployeesInOrganization(1L, "INVALID_AWARD_TYPE"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Unknown award type: INVALID_AWARD_TYPE");

        verify(organizationRepository).findById(1L);
        verify(employeeRepository, never()).findByOrganizationId(any(Long.class));
        verify(employeeRepository, never()).saveAll(anyList());
    }

    @Test
    void testAwardAllEmployeesInOrganization_CaseInsensitiveAwardType() {
        // Given
        Organization testOrganization = Organization.builder()
                .name("Test Organization")
                .build();

        Employee employee1 = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(0)
                .build();

        List<Employee> employees = Arrays.asList(employee1);
        Employee savedEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(testOrganization)
                .dundieAwards(1)
                .build();

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));
        when(employeeRepository.findByOrganizationId(1L)).thenReturn(employees);
        when(employeeRepository.saveAll(anyList())).thenReturn(Arrays.asList(savedEmployee));

        // When - using lowercase award type
        List<EmployeeDto> result = organizationService.awardAllEmployeesInOrganization(1L, "innovation");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(organizationRepository).findById(1L);
        verify(employeeRepository).findByOrganizationId(1L);
        verify(employeeRepository).saveAll(anyList());
        
        // Verify award type was correctly parsed (case-insensitive)
        assertThat(employee1.getAwards()).hasSize(1);
        assertThat(employee1.getAwards().get(0).getType()).isEqualTo(AwardType.INNOVATION);
    }
}

