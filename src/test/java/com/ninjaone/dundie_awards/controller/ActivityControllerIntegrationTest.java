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
        Organization organization = Organization.builder()
                .name("Test Organization")
                .build();
        organization = organizationRepository.save(organization);
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
        employee = employeeRepository.save(employee);
        Activity activity1 = Activity.builder()
                .occurredAt(Instant.now())
                .event(ActivityType.EMPLOYEE_CREATED)
                .employee(employee)
                .build();
        activityRepository.save(activity1);
        Activity activity2 = Activity.builder()
                .occurredAt(Instant.now())
                .event(ActivityType.EMPLOYEE_UPDATED)
                .employee(employee)
                .build();
        activityRepository.save(activity2);

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].id").exists())
                .andExpect(jsonPath("$.items[0].occurredAt").exists())
                .andExpect(jsonPath("$.items[0].employeeId").exists())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void testGetAllActivitiesWhenEmpty() throws Exception {
        activityRepository.deleteAll();

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void testGetAllActivitiesWithPagination() throws Exception {
        // Given - create 5 activities
        Organization organization = Organization.builder()
                .name("Test Organization")
                .build();
        organization = organizationRepository.save(organization);
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
        employee = employeeRepository.save(employee);
        
        for (int i = 0; i < 5; i++) {
            Activity activity = Activity.builder()
                    .occurredAt(Instant.now().plusSeconds(i))
                    .event(ActivityType.EMPLOYEE_CREATED)
                    .employee(employee)
                    .build();
            activityRepository.save(activity);
        }

        // Test first page with size 2
        mockMvc.perform(get("/api/activities")
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
        mockMvc.perform(get("/api/activities")
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
        mockMvc.perform(get("/api/activities")
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
    void testGetAllActivitiesWithSorting() throws Exception {
        // Given - create activities with different timestamps
        Organization organization = Organization.builder()
                .name("Test Organization")
                .build();
        organization = organizationRepository.save(organization);
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .organization(organization)
                .dundieAwards(0)
                .build();
        employee = employeeRepository.save(employee);
        
        Instant baseTime = Instant.now();
        Activity activity1 = Activity.builder()
                .occurredAt(baseTime.plusSeconds(10))
                .event(ActivityType.EMPLOYEE_CREATED)
                .employee(employee)
                .build();
        activityRepository.save(activity1);
        Activity activity2 = Activity.builder()
                .occurredAt(baseTime.plusSeconds(20))
                .event(ActivityType.EMPLOYEE_UPDATED)
                .employee(employee)
                .build();
        activityRepository.save(activity2);

        // Test descending sort (occurredAt,desc) - most recent first
        // activity2 occurred later (baseTime + 20s) so it should be first
        mockMvc.perform(get("/api/activities")
                        .param("sort", "occurredAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].event").value("EMPLOYEE_UPDATED"))
                .andExpect(jsonPath("$.items[1].event").value("EMPLOYEE_CREATED"));

        // Test ascending sort - oldest first
        mockMvc.perform(get("/api/activities")
                        .param("sort", "occurredAt,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].event").value("EMPLOYEE_CREATED"))
                .andExpect(jsonPath("$.items[1].event").value("EMPLOYEE_UPDATED"));
    }
}

