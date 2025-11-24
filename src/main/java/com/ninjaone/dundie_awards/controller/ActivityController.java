package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.ActivityDto;
import com.ninjaone.dundie_awards.dto.ApiError;
import com.ninjaone.dundie_awards.dto.CreateActivityRequest;
import com.ninjaone.dundie_awards.exception.ActivityNotFoundException;
import com.ninjaone.dundie_awards.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService service;

    public ActivityController(ActivityService service) {
        this.service = service;
    }

    @GetMapping
    public List<ActivityDto> all() {
        return service.getAllActivities();
    }

    @GetMapping("/{id}")
    public ActivityDto one(@PathVariable Long id) {
        return service.getActivity(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityDto create(@Valid @RequestBody CreateActivityRequest req) {
        return service.createActivity(req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteActivity(id);
    }

    @ExceptionHandler(ActivityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ActivityNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, ex.getMessage()));
    }
}
