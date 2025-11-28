package com.ninjaone.dundie_awards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.EmployeeRequest;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void testGetAllEmployees() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee1 = new Employee("John", "Doe", organization);
        employee1.setDundieAwards(0);
        employeeRepository.save(employee1);
        Employee employee2 = new Employee("Jane", "Smith", organization);
        employee2.setDundieAwards(1);
        employeeRepository.save(employee2);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetEmployeeById() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);

        mockMvc.perform(get("/api/employees/{id}", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.organizationId").value(organization.getId()))
                .andExpect(jsonPath("$.organizationName").value("Test Organization"))
                .andExpect(jsonPath("$.dundieAwards").value(0));
    }

    @Test
    void testGetEmployeeByIdNotFound() throws Exception {
        Long nonExistentId = 999L;

        mockMvc.perform(get("/api/employees/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee with id 999 not found"));
    }

    @Test
    void testCreateEmployee() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        EmployeeRequest request = new EmployeeRequest("Alice", "Johnson", organization.getId());

        String response = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Johnson"))
                .andExpect(jsonPath("$.organizationId").value(organization.getId()))
                .andExpect(jsonPath("$.organizationName").value("Test Organization"))
                .andExpect(jsonPath("$.dundieAwards").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDto createdEmployee = objectMapper.readValue(response, EmployeeDto.class);
        assertThat(createdEmployee.id()).isNotNull();
        assertThat(employeeRepository.existsById(createdEmployee.id())).isTrue();
    }

    @Test
    void testCreateEmployeeWithInvalidData() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        // Missing firstName
        EmployeeRequest invalidRequest = new EmployeeRequest("", "Johnson", organization.getId());

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateEmployeeWithNonExistentOrganization() throws Exception {
        EmployeeRequest request = new EmployeeRequest("Alice", "Johnson", 999L);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Organization with id 999 not found"));
    }

    @Test
    void testUpdateEmployee() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);
        EmployeeRequest updateRequest = new EmployeeRequest("John", "Updated", organization.getId());

        String response = mockMvc.perform(put("/api/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Updated"))
                .andExpect(jsonPath("$.organizationId").value(organization.getId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDto updatedEmployee = objectMapper.readValue(response, EmployeeDto.class);
        Employee savedEmployee = employeeRepository.findById(updatedEmployee.id()).orElseThrow();
        assertThat(savedEmployee.getLastName()).isEqualTo("Updated");
    }

    @Test
    void testUpdateEmployeeNotFound() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        EmployeeRequest updateRequest = new EmployeeRequest("John", "Updated", organization.getId());

        mockMvc.perform(put("/api/employees/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee with id 999 not found"));
    }

    @Test
    void testUpdateEmployeeWithInvalidData() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);
        EmployeeRequest invalidRequest = new EmployeeRequest("", "Updated", organization.getId());

        mockMvc.perform(put("/api/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteEmployee() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);
        Long employeeId = employee.getId();

        mockMvc.perform(delete("/api/employees/{id}", employeeId))
                .andExpect(status().isNoContent());

        assertThat(employeeRepository.existsById(employeeId)).isFalse();
    }

    @Test
    void testDeleteEmployeeNotFound() throws Exception {
        mockMvc.perform(delete("/api/employees/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee with id 999 not found"));
    }

    @Test
    void testAwardEmployee() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);
        // Initially has 0 awards
        assertThat(employee.getDundieAwards()).isEqualTo(0);

        String response = mockMvc.perform(post("/api/employees/{id}/awards", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.dundieAwards").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDto awardedEmployee = objectMapper.readValue(response, EmployeeDto.class);
        assertThat(awardedEmployee.dundieAwards()).isEqualTo(1);

        // Verify in database
        Employee savedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertThat(savedEmployee.getDundieAwards()).isEqualTo(1);
    }

    @Test
    void testAwardEmployeeMultipleTimes() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);
        // Award first time
        mockMvc.perform(post("/api/employees/{id}/awards", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dundieAwards").value(1));

        // Award second time
        mockMvc.perform(post("/api/employees/{id}/awards", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dundieAwards").value(2));

        // Verify in database
        Employee savedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertThat(savedEmployee.getDundieAwards()).isEqualTo(2);
    }

    @Test
    void testAwardEmployeeWithNullAwards() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        // Create employee with null awards (don't set dundieAwards, leaving it null)
        Employee employeeWithNullAwards = new Employee("Bob", "Wilson", organization);
        // dundieAwards is null by default, so we don't set it
        employeeWithNullAwards = employeeRepository.save(employeeWithNullAwards);

        mockMvc.perform(post("/api/employees/{id}/awards", employeeWithNullAwards.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dundieAwards").value(1));

        // Verify in database
        Employee savedEmployee = employeeRepository.findById(employeeWithNullAwards.getId()).orElseThrow();
        assertThat(savedEmployee.getDundieAwards()).isEqualTo(1);
    }

    @Test
    void testAwardEmployeeNotFound() throws Exception {
        mockMvc.perform(post("/api/employees/{id}/awards", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee with id 999 not found"));
    }
}

