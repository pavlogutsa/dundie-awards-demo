package com.ninjaone.dundie_awards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        Activity activity1 = new Activity(Instant.now(), ActivityType.EMPLOYEE_CREATED, employee);
        activityRepository.save(activity1);
        Activity activity2 = new Activity(Instant.now(), ActivityType.EMPLOYEE_UPDATED, employee);
        activityRepository.save(activity2);

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
        Activity activity = new Activity(Instant.now(), ActivityType.EMPLOYEE_CREATED, employee);
        activity = activityRepository.save(activity);

        mockMvc.perform(get("/api/activities/{id}", activity.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Activity with id 999 not found"));
    }

    @Test
    void testGetAllActivitiesWhenEmpty() throws Exception {
        activityRepository.deleteAll();

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

