package com.ninjaone.dundie_awards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjaone.dundie_awards.dto.AwardRequest;
import com.ninjaone.dundie_awards.model.AwardType;
import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.EmployeeRequest;
import com.ninjaone.dundie_awards.dto.UpdateEmployeeRequest;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import lombok.NonNull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(com.ninjaone.dundie_awards.config.TestSecurityConfig.class)
@SuppressWarnings("null")
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
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Employee employee1 = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
        employeeRepository.save(employee1);
        Employee employee2 = Employee.builder()
                .firstName("Jane")
                .lastName("Smith")
                .organization(organization)
                .dundieAwards(1)
                .build();
        employeeRepository.save(employee2);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void testGetAllEmployeesWithPagination() throws Exception {
        // Given - create 5 employees
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        
        for (int i = 0; i < 5; i++) {
            Employee employee = Employee.builder()
                    .firstName("Employee" + i)
                    .lastName("Last" + i)
                    .organization(organization)
                    .dundieAwards(0)
                    .build();
            employeeRepository.save(employee);
        }

        // Test first page with size 2
        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        // Test second page
        mockMvc.perform(get("/api/employees")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false));

        // Test last page
        mockMvc.perform(get("/api/employees")
                        .param("page", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void testGetAllEmployeesWithSorting() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Employee employee1 = Employee.builder()
                .firstName("Alice")
                .lastName("Zebra")
                .organization(organization)
                .dundieAwards(0)
                .build();
        employeeRepository.save(employee1);
        Employee employee2 = Employee.builder()
                .firstName("Bob")
                .lastName("Alpha")
                .organization(organization)
                .dundieAwards(1)
                .build();
        employeeRepository.save(employee2);

        // Test default sort (id,asc)
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].id").value(employee1.getId()))
                .andExpect(jsonPath("$.items[1].id").value(employee2.getId()));

        // Test sorting by firstName descending
        mockMvc.perform(get("/api/employees")
                        .param("sort", "firstName,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].firstName").value("Bob"))
                .andExpect(jsonPath("$.items[1].firstName").value("Alice"));
    }

    @Test
    void testGetEmployeeById() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
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
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        EmployeeRequest request = new EmployeeRequest("Alice", "Johnson", organization.getId());
        String requestJson = objectMapper.writeValueAsString(request);

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
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        // Missing firstName
        EmployeeRequest invalidRequest = new EmployeeRequest("", "Johnson", organization.getId());
        String invalidRequestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateEmployeeWithNonExistentOrganization() throws Exception {
        EmployeeRequest request = new EmployeeRequest("Alice", "Johnson", 999L);
        String requestJson = objectMapper.writeValueAsString(request);

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
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
        employee = employeeRepository.save(employee);
        EmployeeRequest updateRequest = new EmployeeRequest("John", "Updated", organization.getId());
        String updateRequestJson = objectMapper.writeValueAsString(updateRequest);

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
        @NonNull Employee savedEmployee = employeeRepository.findById(updatedEmployee.id()).orElseThrow();
        assertThat(savedEmployee.getLastName()).isEqualTo("Updated");
    }

    @Test
    void testUpdateEmployeeNotFound() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        EmployeeRequest updateRequest = new EmployeeRequest("John", "Updated", organization.getId());
        String updateRequestJson = objectMapper.writeValueAsString(updateRequest);

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
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
        employee = employeeRepository.save(employee);
        EmployeeRequest invalidRequest = new EmployeeRequest("", "Updated", organization.getId());
        String invalidRequestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(put("/api/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteEmployee() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
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
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
        employee = employeeRepository.save(employee);
        // Initially has 0 awards
        assertThat(employee.getDundieAwards()).isEqualTo(0);

        AwardRequest awardRequest = new AwardRequest(AwardType.INNOVATION);
        String awardRequestJson = objectMapper.writeValueAsString(awardRequest);
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
        @NonNull Employee savedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertThat(savedEmployee.getDundieAwards()).isEqualTo(1);

        // Verify activity was created with the specified type
        List<Activity> activities = activityRepository.findAll();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getEvent()).isEqualTo(ActivityType.AWARD_GRANTED);
        assertThat(activities.get(0).getEmployee().getId()).isEqualTo(employee.getId());
    }

    @Test
    void testAwardEmployeeMultipleTimes() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
        employee = employeeRepository.save(employee);
        // Award first time
        AwardRequest awardRequest1 = new AwardRequest(AwardType.INNOVATION);
        String awardRequest1Json = objectMapper.writeValueAsString(awardRequest1);
        mockMvc.perform(post("/api/employees/{id}/awards", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(awardRequest1Json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dundieAwards").value(1));

        // Award second time
        AwardRequest awardRequest2 = new AwardRequest(AwardType.COMPLETED_PROJECT);
        String awardRequest2Json = objectMapper.writeValueAsString(awardRequest2);
        mockMvc.perform(post("/api/employees/{id}/awards", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(awardRequest2Json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dundieAwards").value(2));

        // Verify in database
        @NonNull Employee savedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertThat(savedEmployee.getDundieAwards()).isEqualTo(2);

        // Verify activities were created with the specified types
        List<Activity> activities = activityRepository.findAll();
        Long employeeId = employee.getId();
        assertThat(activities).hasSize(2);
        assertThat(activities.stream().map(Activity::getEvent).toList())
                .containsExactlyInAnyOrder(ActivityType.AWARD_GRANTED, ActivityType.AWARD_GRANTED);
        assertThat(activities).allMatch(a -> employeeId.equals(a.getEmployee().getId()));
    }

    @Test
    void testAwardEmployeeWithNullAwards() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        // Create employee with null awards (don't set dundieAwards, leaving it null)
        Employee employeeWithNullAwards = Employee.builder()
                .firstName("Bob")
                .lastName("Wilson")
                .organization(organization)
                .build();
        // dundieAwards is null by default, so we don't set it
        employeeWithNullAwards = employeeRepository.save(employeeWithNullAwards);

        AwardRequest awardRequest = new AwardRequest(AwardType.INNOVATION);
        String awardRequestJson = objectMapper.writeValueAsString(awardRequest);
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
        assertThat(activities.get(0).getEvent()).isEqualTo(ActivityType.AWARD_GRANTED);
    }

    @Test
    void testAwardEmployeeNotFound() throws Exception {
        AwardRequest awardRequest = new AwardRequest(AwardType.INNOVATION);
        String awardRequestJson = objectMapper.writeValueAsString(awardRequest);
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
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(2)
                .build();
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
        @NonNull Employee savedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
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
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
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

    @Test
    void testPatchEmployeeWithFirstNameOnly() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
        employee = employeeRepository.save(employee);
        UpdateEmployeeRequest patchRequest = new UpdateEmployeeRequest("Jane", null, null);
        String patchRequestJson = objectMapper.writeValueAsString(patchRequest);

        String response = mockMvc.perform(patch("/api/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(patchRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe")) // Should remain unchanged
                .andExpect(jsonPath("$.organizationId").value(organization.getId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDto patchedEmployee = objectMapper.readValue(response, EmployeeDto.class);
        @NonNull Employee savedEmployee = employeeRepository.findById(patchedEmployee.id()).orElseThrow();
        assertThat(savedEmployee.getFirstName()).isEqualTo("Jane");
        assertThat(savedEmployee.getLastName()).isEqualTo("Doe");
    }

    @Test
    void testPatchEmployeeWithLastNameOnly() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
        employee = employeeRepository.save(employee);
        UpdateEmployeeRequest patchRequest = new UpdateEmployeeRequest(null, "Smith", null);
        String patchRequestJson = objectMapper.writeValueAsString(patchRequest);

        String response = mockMvc.perform(patch("/api/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(patchRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.firstName").value("John")) // Should remain unchanged
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.organizationId").value(organization.getId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDto patchedEmployee = objectMapper.readValue(response, EmployeeDto.class);
        @NonNull Employee savedEmployee = employeeRepository.findById(patchedEmployee.id()).orElseThrow();
        assertThat(savedEmployee.getFirstName()).isEqualTo("John");
        assertThat(savedEmployee.getLastName()).isEqualTo("Smith");
    }

    @Test
    void testPatchEmployeeWithOrganizationIdOnly() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Organization newOrganization = Organization.builder()
                .name("New Organization")
                .build();
        newOrganization = organizationRepository.save(newOrganization);
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
        employee = employeeRepository.save(employee);
        UpdateEmployeeRequest patchRequest = new UpdateEmployeeRequest(null, null, newOrganization.getId());
        String patchRequestJson = objectMapper.writeValueAsString(patchRequest);

        String response = mockMvc.perform(patch("/api/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(patchRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.firstName").value("John")) // Should remain unchanged
                .andExpect(jsonPath("$.lastName").value("Doe")) // Should remain unchanged
                .andExpect(jsonPath("$.organizationId").value(newOrganization.getId()))
                .andExpect(jsonPath("$.organizationName").value("New Organization"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDto patchedEmployee = objectMapper.readValue(response, EmployeeDto.class);
        @NonNull Employee savedEmployee = employeeRepository.findById(patchedEmployee.id()).orElseThrow();
        assertThat(savedEmployee.getFirstName()).isEqualTo("John");
        assertThat(savedEmployee.getLastName()).isEqualTo("Doe");
        assertThat(savedEmployee.getOrganization().getId()).isEqualTo(newOrganization.getId());
    }

    @Test
    void testPatchEmployeeWithMultipleFields() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Organization newOrganization = Organization.builder()
                .name("New Organization")
                .build();
        newOrganization = organizationRepository.save(newOrganization);
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
        employee = employeeRepository.save(employee);
        UpdateEmployeeRequest patchRequest = new UpdateEmployeeRequest("Jane", "Smith", newOrganization.getId());
        String patchRequestJson = objectMapper.writeValueAsString(patchRequest);

        String response = mockMvc.perform(patch("/api/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(patchRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.organizationId").value(newOrganization.getId()))
                .andExpect(jsonPath("$.organizationName").value("New Organization"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDto patchedEmployee = objectMapper.readValue(response, EmployeeDto.class);
        @NonNull Employee savedEmployee = employeeRepository.findById(patchedEmployee.id()).orElseThrow();
        assertThat(savedEmployee.getFirstName()).isEqualTo("Jane");
        assertThat(savedEmployee.getLastName()).isEqualTo("Smith");
        assertThat(savedEmployee.getOrganization().getId()).isEqualTo(newOrganization.getId());
    }

    @Test
    void testPatchEmployeeWithAllNullFields() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
        employee = employeeRepository.save(employee);
        UpdateEmployeeRequest patchRequest = new UpdateEmployeeRequest(null, null, null);
        String patchRequestJson = objectMapper.writeValueAsString(patchRequest);

        String response = mockMvc.perform(patch("/api/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(patchRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.firstName").value("John")) // Should remain unchanged
                .andExpect(jsonPath("$.lastName").value("Doe")) // Should remain unchanged
                .andExpect(jsonPath("$.organizationId").value(organization.getId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDto patchedEmployee = objectMapper.readValue(response, EmployeeDto.class);
        @NonNull Employee savedEmployee = employeeRepository.findById(patchedEmployee.id()).orElseThrow();
        assertThat(savedEmployee.getFirstName()).isEqualTo("John");
        assertThat(savedEmployee.getLastName()).isEqualTo("Doe");
    }

    @Test
    void testPatchEmployeeNotFound() throws Exception {
        UpdateEmployeeRequest patchRequest = new UpdateEmployeeRequest("Jane", null, null);
        String patchRequestJson = objectMapper.writeValueAsString(patchRequest);

        mockMvc.perform(patch("/api/employees/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(patchRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee with id 999 not found"));
    }

    @Test
    void testPatchEmployeeWithNonExistentOrganization() throws Exception {
        // Given
        @NonNull Organization organization = organizationRepository.save(Organization.builder()
                .name("Test Organization")
                .build());
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
        employee = employeeRepository.save(employee);
        UpdateEmployeeRequest patchRequest = new UpdateEmployeeRequest(null, null, 999L);
        String patchRequestJson = objectMapper.writeValueAsString(patchRequest);

        mockMvc.perform(patch("/api/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(patchRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Organization with id 999 not found"));
    }
}

