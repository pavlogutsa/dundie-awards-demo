package com.ninjaone.dundie_awards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjaone.dundie_awards.dto.AwardRequest;
import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.EmployeeRequest;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.model.ActivityType;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
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

import java.util.List;
import java.util.Objects;

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

    @Autowired
    private ActivityRepository activityRepository;

    @BeforeEach
    void setUp() {
        activityRepository.deleteAll();
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee with id 999 not found"));
    }

    @Test
    void testCreateEmployee() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        EmployeeRequest request = new EmployeeRequest("Alice", "Johnson", organization.getId());
        String requestJson = Objects.requireNonNull(objectMapper.writeValueAsString(request));

        String response = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
        String invalidRequestJson = Objects.requireNonNull(objectMapper.writeValueAsString(invalidRequest));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateEmployeeWithNonExistentOrganization() throws Exception {
        EmployeeRequest request = new EmployeeRequest("Alice", "Johnson", 999L);
        String requestJson = Objects.requireNonNull(objectMapper.writeValueAsString(request));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
        String updateRequestJson = Objects.requireNonNull(objectMapper.writeValueAsString(updateRequest));

        String response = mockMvc.perform(put("/api/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(updateRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
        String updateRequestJson = Objects.requireNonNull(objectMapper.writeValueAsString(updateRequest));

        mockMvc.perform(put("/api/employees/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(updateRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
        String invalidRequestJson = Objects.requireNonNull(objectMapper.writeValueAsString(invalidRequest));

        mockMvc.perform(put("/api/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(invalidRequestJson))
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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

        AwardRequest awardRequest = new AwardRequest(ActivityType.HELPED_TEAMMATE);
        String awardRequestJson = Objects.requireNonNull(objectMapper.writeValueAsString(awardRequest));
        String response = mockMvc.perform(post("/api/employees/{id}/awards", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(awardRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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

        // Verify activity was created with the specified type
        List<Activity> activities = activityRepository.findAll();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getEvent()).isEqualTo(ActivityType.HELPED_TEAMMATE);
        assertThat(activities.get(0).getEmployee().getId()).isEqualTo(employee.getId());
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
        AwardRequest awardRequest1 = new AwardRequest(ActivityType.COMPLETED_PROJECT);
        String awardRequest1Json = Objects.requireNonNull(objectMapper.writeValueAsString(awardRequest1));
        mockMvc.perform(post("/api/employees/{id}/awards", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(awardRequest1Json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dundieAwards").value(1));

        // Award second time
        AwardRequest awardRequest2 = new AwardRequest(ActivityType.MENTORED_COLLEAGUE);
        String awardRequest2Json = Objects.requireNonNull(objectMapper.writeValueAsString(awardRequest2));
        mockMvc.perform(post("/api/employees/{id}/awards", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(awardRequest2Json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dundieAwards").value(2));

        // Verify in database
        Employee savedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertThat(savedEmployee.getDundieAwards()).isEqualTo(2);

        // Verify activities were created with the specified types
        List<Activity> activities = activityRepository.findAll();
        Long employeeId = employee.getId();
        assertThat(activities).hasSize(2);
        assertThat(activities.stream().map(Activity::getEvent).toList())
                .containsExactlyInAnyOrder(ActivityType.COMPLETED_PROJECT, ActivityType.MENTORED_COLLEAGUE);
        assertThat(activities).allMatch(a -> a.getEmployee().getId() == employeeId);
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

        AwardRequest awardRequest = new AwardRequest(ActivityType.INNOVATION);
        String awardRequestJson = Objects.requireNonNull(objectMapper.writeValueAsString(awardRequest));
        mockMvc.perform(post("/api/employees/{id}/awards", employeeWithNullAwards.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(awardRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dundieAwards").value(1));

        // Verify in database
        Employee savedEmployee = employeeRepository.findById(employeeWithNullAwards.getId()).orElseThrow();
        assertThat(savedEmployee.getDundieAwards()).isEqualTo(1);

        // Verify activity was created with the specified type
        List<Activity> activities = activityRepository.findAll();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getEvent()).isEqualTo(ActivityType.INNOVATION);
    }

    @Test
    void testAwardEmployeeNotFound() throws Exception {
        AwardRequest awardRequest = new AwardRequest(ActivityType.CUSTOMER_SATISFACTION);
        String awardRequestJson = Objects.requireNonNull(objectMapper.writeValueAsString(awardRequest));
        mockMvc.perform(post("/api/employees/{id}/awards", 999L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(awardRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee with id 999 not found"));
    }

    @Test
    void testRemoveAward() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(2);
        employee = employeeRepository.save(employee);

        String response = mockMvc.perform(delete("/api/employees/{id}/awards", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.dundieAwards").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDto updatedEmployee = objectMapper.readValue(response, EmployeeDto.class);
        assertThat(updatedEmployee.dundieAwards()).isEqualTo(1);

        // Verify in database
        Employee savedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertThat(savedEmployee.getDundieAwards()).isEqualTo(1);

        // Verify activity was created
        List<Activity> activities = activityRepository.findAll();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getEvent()).isEqualTo(ActivityType.AWARD_REMOVED);
        assertThat(activities.get(0).getEmployee().getId()).isEqualTo(employee.getId());
    }

    @Test
    void testRemoveAwardWithNoAwards() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);

        mockMvc.perform(delete("/api/employees/{id}/awards", employee.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Employee has no awards to remove"));
    }

    @Test
    void testRemoveAwardNotFound() throws Exception {
        mockMvc.perform(delete("/api/employees/{id}/awards", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee with id 999 not found"));
    }
}

