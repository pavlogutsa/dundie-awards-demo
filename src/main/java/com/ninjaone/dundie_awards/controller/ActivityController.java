package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.ActivityDto;
import com.ninjaone.dundie_awards.dto.PageResponse;
import com.ninjaone.dundie_awards.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService service;

    public ActivityController(ActivityService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ActivityDto> getAllActivities(Pageable pageable) {
        log.info("GET /api/activities - page={}, size={}, sort={}", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return PageResponse.from(service.getAllActivities(pageable));
    }
}
