package com.hamza.taskmanager.controller;

import com.hamza.taskmanager.dto.common.ApiSuccessResponse;
import com.hamza.taskmanager.dto.task.TaskCreateRequest;
import com.hamza.taskmanager.dto.task.TaskResponse;
import com.hamza.taskmanager.dto.task.TaskUpdateRequest;
import com.hamza.taskmanager.enums.TaskStatus;
import com.hamza.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<TaskResponse>> createTask(@Valid @RequestBody TaskCreateRequest request) {
        return ResponseEntity.ok(ApiSuccessResponse.<TaskResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Task created successfully")
                .data(taskService.createTask(request))
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<Page<TaskResponse>>> getAllTasks(@RequestParam(required = false) TaskStatus status, Pageable pageable) {
        return ResponseEntity.ok(ApiSuccessResponse.<Page<TaskResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Tasks fetched successfully")
                .data(taskService.getAllTasks(status, pageable))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<TaskResponse>> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiSuccessResponse.<TaskResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Task fetched successfully")
                .data(taskService.getTaskById(id))
                .build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiSuccessResponse<List<TaskResponse>>> getTasksByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiSuccessResponse.<List<TaskResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("User tasks fetched successfully")
                .data(taskService.getTasksByUserId(userId))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<TaskResponse>> updateTask(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(ApiSuccessResponse.<TaskResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Task updated successfully")
                .data(taskService.updateTask(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiSuccessResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Task deleted successfully")
                .build());
    }
}
