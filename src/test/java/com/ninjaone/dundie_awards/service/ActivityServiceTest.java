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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import lombok.NonNull;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
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
        @NonNull Pageable pageable = PageRequest.of(0, 20);
        @NonNull Page<Activity> activityPage = new PageImpl<>(activities, pageable, activities.size());

        when(activityRepository.findAll(any(Pageable.class))).thenReturn(activityPage);

        // When
        Page<ActivityDto> result = activityService.getAllActivities(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        // Use real mapper to create expected DTOs for comparison
        List<ActivityDto> expectedDtos = activityMapper.toDtoList(activities);
        assertThat(result.getContent()).isEqualTo(expectedDtos);
        verify(activityRepository).findAll(any(Pageable.class));
    }

    @Test
    void testGetAllActivitiesWhenEmpty() {
        // Given
        @NonNull Pageable pageable = PageRequest.of(0, 20);
        @NonNull Page<Activity> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(activityRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // When
        Page<ActivityDto> result = activityService.getAllActivities(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
        verify(activityRepository).findAll(any(Pageable.class));
    }

    @Test
    void testGetAllActivitiesWithPagination() {
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
        
        List<Activity> activities = Arrays.asList(
                Activity.builder()
                        .occurredAt(Instant.now())
                        .event(ActivityType.EMPLOYEE_CREATED)
                        .employee(testEmployee)
                        .build(),
                Activity.builder()
                        .occurredAt(Instant.now())
                        .event(ActivityType.EMPLOYEE_UPDATED)
                        .employee(testEmployee)
                        .build()
        );
        
        @NonNull Pageable pageable = PageRequest.of(0, 1);
        @NonNull Page<Activity> firstPage = new PageImpl<>(activities.subList(0, 1), pageable, 2);

        when(activityRepository.findAll(any(Pageable.class))).thenReturn(firstPage);

        // When
        Page<ActivityDto> result = activityService.getAllActivities(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isFalse();
        verify(activityRepository).findAll(any(Pageable.class));
    }
}

