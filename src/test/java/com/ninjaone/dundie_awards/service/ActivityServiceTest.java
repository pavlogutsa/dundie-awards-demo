package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.ActivityDto;
import com.ninjaone.dundie_awards.dto.CreateActivityRequest;
import com.ninjaone.dundie_awards.exception.ActivityNotFoundException;
import com.ninjaone.dundie_awards.exception.EmployeeNotFoundException;
import com.ninjaone.dundie_awards.mapper.ActivityMapper;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ActivityMapper activityMapper;

    @InjectMocks
    private ActivityService activityService;

    private Organization testOrganization;
    private Employee testEmployee;
    private Activity testActivity;
    private ActivityDto testActivityDto;
    private CreateActivityRequest testCreateRequest;

    @BeforeEach
    void setUp() {
        testOrganization = new Organization("Test Organization");
        testOrganization.setId(1L);

        testEmployee = new Employee("John", "Doe", testOrganization);
        testEmployee.setId(1L);
        testEmployee.setDundieAwards(0);

        Instant occurredAt = Instant.now();
        testActivity = new Activity(occurredAt, "EMPLOYEE_CREATED", testEmployee);
        testActivity.setId(1L);

        testActivityDto = new ActivityDto(1L, occurredAt, 1L, "EMPLOYEE_CREATED");

        testCreateRequest = new CreateActivityRequest(1L, occurredAt, "EMPLOYEE_CREATED");
    }

    @Test
    void testGetAllActivities() {
        // Given
        Activity activity = new Activity(Instant.now(), "EMPLOYEE_UPDATED", testEmployee);
        activity.setId(2L);
        List<Activity> activities = Arrays.asList(testActivity, activity);

        ActivityDto activityDto = new ActivityDto(2L, activity.getOccurredAt(), 1L, "EMPLOYEE_UPDATED");
        List<ActivityDto> expectedDtos = Arrays.asList(testActivityDto, activityDto);

        when(activityRepository.findAll()).thenReturn(activities);
        when(activityMapper.toDtoList(activities)).thenReturn(expectedDtos);

        // When
        List<ActivityDto> result = activityService.getAllActivities();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedDtos);
        verify(activityRepository).findAll();
        verify(activityMapper).toDtoList(activities);
    }

    @Test
    void testGetAllActivitiesWhenEmpty() {
        // Given
        when(activityRepository.findAll()).thenReturn(List.of());
        when(activityMapper.toDtoList(any())).thenReturn(List.of());

        // When
        List<ActivityDto> result = activityService.getAllActivities();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(activityRepository).findAll();
        verify(activityMapper).toDtoList(any());
    }

    @Test
    void testGetActivityById() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(activityMapper.toDto(testActivity)).thenReturn(testActivityDto);

        // When
        ActivityDto result = activityService.getActivity(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testActivityDto);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.employeeId()).isEqualTo(1L);
        verify(activityRepository).findById(1L);
        verify(activityMapper).toDto(testActivity);
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
        verify(activityMapper, never()).toDto(any());
    }

    @Test
    void testCreateActivity() {
        // Given
        Activity newActivity = new Activity();
        newActivity.setEmployee(testEmployee);
        newActivity.setOccurredAt(testCreateRequest.occurredAt());
        newActivity.setEvent(testCreateRequest.event());

        Activity savedActivity = new Activity(testCreateRequest.occurredAt(), "EMPLOYEE_CREATED", testEmployee);
        savedActivity.setId(1L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity activity = invocation.getArgument(0);
            assertThat(activity.getEvent()).isEqualTo("EMPLOYEE_CREATED");
            return savedActivity;
        });
        when(activityMapper.toDto(savedActivity)).thenReturn(testActivityDto);

        // When
        ActivityDto result = activityService.createActivity(testCreateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testActivityDto);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.employeeId()).isEqualTo(1L);
        assertThat(result.event()).isEqualTo("EMPLOYEE_CREATED");
        verify(employeeRepository).findById(1L);
        verify(activityRepository).save(any(Activity.class));
        verify(activityMapper).toDto(savedActivity);
    }

    @Test
    void testCreateActivityWithNonExistentEmployee() {
        // Given
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        CreateActivityRequest request = new CreateActivityRequest(999L, Instant.now(), "EMPLOYEE_CREATED");

        // When/Then
        assertThatThrownBy(() -> activityService.createActivity(request))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee with id 999 not found");

        verify(employeeRepository).findById(999L);
        verify(activityRepository, never()).save(any());
        verify(activityMapper, never()).toDto(any());
    }

    @Test
    void testCreateActivitySetsEmployeeAndOccurredAt() {
        // Given
        Instant specificTime = Instant.parse("2024-01-01T12:00:00Z");
        CreateActivityRequest request = new CreateActivityRequest(1L, specificTime, "EMPLOYEE_CREATED");

        Activity savedActivity = new Activity(specificTime, "EMPLOYEE_CREATED", testEmployee);
        savedActivity.setId(1L);

        ActivityDto savedDto = new ActivityDto(1L, specificTime, 1L, "EMPLOYEE_CREATED");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity activity = invocation.getArgument(0);
            assertThat(activity.getEmployee()).isEqualTo(testEmployee);
            assertThat(activity.getOccurredAt()).isEqualTo(specificTime);
            assertThat(activity.getEvent()).isEqualTo("EMPLOYEE_CREATED");
            return savedActivity;
        });
        when(activityMapper.toDto(savedActivity)).thenReturn(savedDto);

        // When
        ActivityDto result = activityService.createActivity(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.occurredAt()).isEqualTo(specificTime);
        verify(employeeRepository).findById(1L);
        verify(activityRepository).save(any(Activity.class));
        verify(activityMapper).toDto(savedActivity);
    }

    @Test
    void testDeleteActivity() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        doNothing().when(activityRepository).delete(testActivity);

        // When
        activityService.deleteActivity(1L);

        // Then
        verify(activityRepository).findById(1L);
        verify(activityRepository).delete(testActivity);
    }

    @Test
    void testDeleteActivityNotFound() {
        // Given
        when(activityRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> activityService.deleteActivity(999L))
                .isInstanceOf(ActivityNotFoundException.class)
                .hasMessage("Activity with id 999 not found");

        verify(activityRepository).findById(999L);
        verify(activityRepository, never()).delete(any());
    }

    @Test
    void testCreateActivityWithFutureDate() {
        // Given
        Instant futureDate = Instant.now().plusSeconds(86400); // 1 day in seconds
        CreateActivityRequest request = new CreateActivityRequest(1L, futureDate, "EMPLOYEE_CREATED");

        Activity savedActivity = new Activity(futureDate, "EMPLOYEE_CREATED", testEmployee);
        savedActivity.setId(1L);

        ActivityDto savedDto = new ActivityDto(1L, futureDate, 1L, "EMPLOYEE_CREATED");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(activityRepository.save(any(Activity.class))).thenReturn(savedActivity);
        when(activityMapper.toDto(savedActivity)).thenReturn(savedDto);

        // When
        ActivityDto result = activityService.createActivity(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.occurredAt()).isEqualTo(futureDate);
        verify(employeeRepository).findById(1L);
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void testCreateActivityWithPastDate() {
        // Given
        Instant pastDate = Instant.now().minusSeconds(86400); // 1 day in seconds
        CreateActivityRequest request = new CreateActivityRequest(1L, pastDate, "EMPLOYEE_CREATED");

        Activity savedActivity = new Activity(pastDate, "EMPLOYEE_CREATED", testEmployee);
        savedActivity.setId(1L);

        ActivityDto savedDto = new ActivityDto(1L, pastDate, 1L, "EMPLOYEE_CREATED");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(activityRepository.save(any(Activity.class))).thenReturn(savedActivity);
        when(activityMapper.toDto(savedActivity)).thenReturn(savedDto);

        // When
        ActivityDto result = activityService.createActivity(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.occurredAt()).isEqualTo(pastDate);
        verify(employeeRepository).findById(1L);
        verify(activityRepository).save(any(Activity.class));
    }
}

