package com.ninjaone.dundie_awards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ninjaone.dundie_awards.dto.ActivityDto;
import com.ninjaone.dundie_awards.dto.CreateActivityRequest;
import com.ninjaone.dundie_awards.model.Activity;
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

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ActivityControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @BeforeEach
    void setUp() {
        // Configure ObjectMapper to handle Instant
        objectMapper.registerModule(new JavaTimeModule());

        activityRepository.deleteAll();
        employeeRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void testGetAllActivities() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);
        Activity activity1 = new Activity(Instant.now(), "EMPLOYEE_CREATED", employee);
        activityRepository.save(activity1);
        Activity activity2 = new Activity(Instant.now(), "EMPLOYEE_UPDATED", employee);
        activityRepository.save(activity2);

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].occurredAt").exists())
                .andExpect(jsonPath("$[0].employeeId").exists());
    }

    @Test
    void testGetActivityById() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);
        Activity activity = new Activity(Instant.now(), "EMPLOYEE_CREATED", employee);
        activity = activityRepository.save(activity);

        mockMvc.perform(get("/api/activities/{id}", activity.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(activity.getId()))
                .andExpect(jsonPath("$.occurredAt").exists())
                .andExpect(jsonPath("$.employeeId").value(employee.getId()))
                .andExpect(jsonPath("$.event").value("EMPLOYEE_CREATED"));
    }

    @Test
    void testGetActivityByIdNotFound() throws Exception {
        Long nonExistentId = 999L;

        mockMvc.perform(get("/api/activities/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Activity with id 999 not found"));
    }

    @Test
    void testCreateActivity() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);
        Instant occurredAt = Instant.now();
        CreateActivityRequest request = new CreateActivityRequest(employee.getId(), occurredAt, "EMPLOYEE_CREATED");

        String response = mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.occurredAt").exists())
                .andExpect(jsonPath("$.employeeId").value(employee.getId()))
                .andExpect(jsonPath("$.event").value("EMPLOYEE_CREATED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ActivityDto createdActivity = objectMapper.readValue(response, ActivityDto.class);
        assertThat(createdActivity.id()).isNotNull();
        assertThat(activityRepository.existsById(createdActivity.id())).isTrue();

        // Verify the activity is linked to the correct employee
        Activity savedActivity = activityRepository.findById(createdActivity.id()).orElseThrow();
        assertThat(savedActivity.getEmployee().getId()).isEqualTo(employee.getId());
        assertThat(savedActivity.getOccurredAt()).isNotNull();
        assertThat(savedActivity.getEvent()).isEqualTo("EMPLOYEE_CREATED");
    }

    @Test
    void testCreateActivityWithInvalidData() throws Exception {
        // Missing employeeId
        CreateActivityRequest invalidRequest = new CreateActivityRequest(null, Instant.now(), "EMPLOYEE_CREATED");

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateActivityWithNullOccurredAt() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);
        // Missing occurredAt
        CreateActivityRequest invalidRequest = new CreateActivityRequest(employee.getId(), null, "EMPLOYEE_CREATED");

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateActivityWithBlankEvent() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);
        // Blank event
        CreateActivityRequest invalidRequest = new CreateActivityRequest(employee.getId(), Instant.now(), "");

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateActivityWithNullEvent() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);
        // Null event
        CreateActivityRequest invalidRequest = new CreateActivityRequest(employee.getId(), Instant.now(), null);

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateActivityWithNonExistentEmployee() throws Exception {
        CreateActivityRequest request = new CreateActivityRequest(999L, Instant.now(), "EMPLOYEE_CREATED");

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee with id 999 not found"));
    }

    @Test
    void testDeleteActivity() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);
        Activity activity = new Activity(Instant.now(), "EMPLOYEE_CREATED", employee);
        activity = activityRepository.save(activity);
        Long activityId = activity.getId();

        mockMvc.perform(delete("/api/activities/{id}", activityId))
                .andExpect(status().isNoContent());

        assertThat(activityRepository.existsById(activityId)).isFalse();
    }

    @Test
    void testDeleteActivityNotFound() throws Exception {
        mockMvc.perform(delete("/api/activities/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Activity with id 999 not found"));
    }

    @Test
    void testGetAllActivitiesWhenEmpty() throws Exception {
        activityRepository.deleteAll();

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testCreateActivityWithFutureDate() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);
        Instant futureDate = Instant.now().plusSeconds(86400); // 1 day in seconds
        CreateActivityRequest request = new CreateActivityRequest(employee.getId(), futureDate, "EMPLOYEE_CREATED");

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.occurredAt").exists())
                .andExpect(jsonPath("$.event").value("EMPLOYEE_CREATED"));
    }

    @Test
    void testCreateActivityWithPastDate() throws Exception {
        // Given
        Organization organization = new Organization("Test Organization");
        organization = organizationRepository.save(organization);
        Employee employee = new Employee("John", "Doe", organization);
        employee.setDundieAwards(0);
        employee = employeeRepository.save(employee);
        Instant pastDate = Instant.now().minusSeconds(86400); // 1 day in seconds
        CreateActivityRequest request = new CreateActivityRequest(employee.getId(), pastDate, "EMPLOYEE_CREATED");

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.occurredAt").exists())
                .andExpect(jsonPath("$.event").value("EMPLOYEE_CREATED"));
    }
}

