package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.ActivityDto;
import com.ninjaone.dundie_awards.dto.CreateActivityRequest;
import com.ninjaone.dundie_awards.exception.ActivityNotFoundException;
import com.ninjaone.dundie_awards.exception.EmployeeNotFoundException;
import com.ninjaone.dundie_awards.mapper.ActivityMapper;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final EmployeeRepository employeeRepository;
    private final ActivityMapper activityMapper;

    public ActivityService(ActivityRepository activityRepository,
                           EmployeeRepository employeeRepository,
                           ActivityMapper activityMapper) {
        this.activityRepository = activityRepository;
        this.employeeRepository = employeeRepository;
        this.activityMapper = activityMapper;
    }

    @Transactional(readOnly=true)
    public List<ActivityDto> getAllActivities() {
        return activityMapper.toDtoList(activityRepository.findAll());
    }

    @Transactional(readOnly=true)
    public ActivityDto getActivity(Long id) {
        Activity a = activityRepository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException(id));
        return activityMapper.toDto(a);
    }

    public ActivityDto createActivity(CreateActivityRequest req) {
        Employee e = employeeRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(req.getEmployeeId()));

        Activity a = new Activity();
        a.setEmployee(e);
        a.setOccurredAt(req.getOccurredAt());

        return activityMapper.toDto(activityRepository.save(a));
    }

    public void deleteActivity(Long id) {
        Activity a = activityRepository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException(id));
        activityRepository.delete(a);
    }
}
