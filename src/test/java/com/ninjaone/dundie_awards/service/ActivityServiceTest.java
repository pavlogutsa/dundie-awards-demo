package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.ActivityDto;
import com.ninjaone.dundie_awards.exception.ActivityNotFoundException;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        // Use real MapStruct mapper instead of mock
        activityMapper = new com.ninjaone.dundie_awards.mapper.ActivityMapperImpl();
        // Manually inject the real mapper into the service since @InjectMocks won't work with manual initialization
        activityService = new ActivityService(activityRepository, activityMapper);
    }

    @Test
    void testGetAllActivities() {
        // Given
        Organization testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);
        Employee testEmployee = new Employee("John", "Doe", testOrganization);
        testEmployee.setId(1L);
        testEmployee.setDundieAwards(0);
        
        Instant occurredAt = Instant.now();
        Activity testActivity = new Activity(occurredAt, ActivityType.EMPLOYEE_CREATED, testEmployee);
        testActivity.setId(1L);
        
        Activity activity = new Activity(Instant.now(), ActivityType.EMPLOYEE_UPDATED, testEmployee);
        activity.setId(2L);
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

    @Test
    void testGetActivityById() {
        // Given
        Organization testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);
        Employee testEmployee = new Employee("John", "Doe", testOrganization);
        testEmployee.setId(1L);
        testEmployee.setDundieAwards(0);
        
        Instant occurredAt = Instant.now();
        Activity testActivity = new Activity(occurredAt, ActivityType.EMPLOYEE_CREATED, testEmployee);
        testActivity.setId(1L);
        ActivityDto testActivityDto = activityMapper.toDto(testActivity);
        
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));

        // When
        ActivityDto result = activityService.getActivity(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testActivityDto);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.employeeId()).isEqualTo(1L);
        verify(activityRepository).findById(1L);
    }

    @Test
    void testGetActivityByIdNotFound() {
        // Given
        when(activityRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> activityService.getActivity(999L))
                .isInstanceOf(ActivityNotFoundException.class)
                .hasMessage("Activity with id 999 not found");

        verify(activityRepository).findById(999L);
    }
}

