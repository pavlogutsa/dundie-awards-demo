package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.ActivityDto;
import com.ninjaone.dundie_awards.dto.PageResponse;
import com.ninjaone.dundie_awards.service.ActivityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService service;

    public ActivityController(ActivityService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ActivityDto> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "occurredAt,desc") String sort) {

        Sort sortSpec = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortSpec);

        Page<ActivityDto> pageResult = service.getAllActivities(pageable);
        return PageResponse.from(pageResult);
    }

    private Sort parseSort(String sort) {
        // expected format: "field,direction"
        String[] parts = sort.split(",");
        if (parts.length != 2) {
            return Sort.by("occurredAt").descending();
        }
        String field = parts[0];
        String direction = parts[1].toLowerCase();
        return "desc".equals(direction)
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
    }
}
