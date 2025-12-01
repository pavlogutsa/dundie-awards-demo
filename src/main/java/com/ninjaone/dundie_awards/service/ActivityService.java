package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.ActivityDto;
import com.ninjaone.dundie_awards.mapper.ActivityMapper;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    public Page<ActivityDto> getAllActivities(@NonNull Pageable pageable) {
        log.debug("Getting all activities with pagination: page={}, size={}, sort={}", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<ActivityDto> result = activityRepository.findAll(pageable)
                .map(activityMapper::toDto);
        log.debug("Retrieved {} activities (total: {})", result.getNumberOfElements(), result.getTotalElements());
        return result;
    }
}
