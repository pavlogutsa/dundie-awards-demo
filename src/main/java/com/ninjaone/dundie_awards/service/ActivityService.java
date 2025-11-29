package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.ActivityDto;
import com.ninjaone.dundie_awards.exception.ActivityNotFoundException;
import com.ninjaone.dundie_awards.mapper.ActivityMapper;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;

    public ActivityService(ActivityRepository activityRepository,
                           ActivityMapper activityMapper) {
        this.activityRepository = activityRepository;
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
}
