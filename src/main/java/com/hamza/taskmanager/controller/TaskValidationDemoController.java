package com.hamza.taskmanager.controller;

import com.hamza.taskmanager.dto.task.TaskCreateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demo/tasks")
public class TaskValidationDemoController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> validateTaskRequest(@Valid @RequestBody TaskCreateRequest request) {
        return Map.of(
                "message", "Request is valid",
                "title", request.getTitle(),
                "userId", request.getUserId()
        );
    }
}
