package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.ActivityDto;
import com.ninjaone.dundie_awards.mapper.ActivityMapper;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.model.ActivityType;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private ActivityMapper activityMapper;

    private ActivityService activityService;

    @BeforeEach
    void setUp() {
        activityMapper = Mappers.getMapper(ActivityMapper.class);
        activityService = new ActivityService(activityRepository, activityMapper);
    }

    @Test
    void testGetAllActivities() {
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
        
        Instant occurredAt = Instant.now();
        Activity testActivity = Activity.builder()
                .occurredAt(occurredAt)
                .event(ActivityType.EMPLOYEE_CREATED)
                .employee(testEmployee)
                .build();
        
        Activity activity = Activity.builder()
                .occurredAt(Instant.now())
                .event(ActivityType.EMPLOYEE_UPDATED)
                .employee(testEmployee)
                .build();
        List<Activity> activities = Arrays.asList(testActivity, activity);

        when(activityRepository.findAll()).thenReturn(activities);

        // When
        List<ActivityDto> result = activityService.getAllActivities();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        // Use real mapper to create expected DTOs for comparison
        List<ActivityDto> expectedDtos = activityMapper.toDtoList(activities);
        assertThat(result).isEqualTo(expectedDtos);
        verify(activityRepository).findAll();
    }

    @Test
    void testGetAllActivitiesWhenEmpty() {
        // Given
        when(activityRepository.findAll()).thenReturn(List.of());

        // When
        List<ActivityDto> result = activityService.getAllActivities();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(activityRepository).findAll();
    }
}

