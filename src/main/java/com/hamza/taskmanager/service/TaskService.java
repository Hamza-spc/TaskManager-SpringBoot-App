package com.hamza.taskmanager.service;

import com.hamza.taskmanager.dto.task.TaskCreateRequest;
import com.hamza.taskmanager.dto.task.TaskResponse;
import com.hamza.taskmanager.dto.task.TaskUpdateRequest;
import com.hamza.taskmanager.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(TaskCreateRequest request);
    Page<TaskResponse> getAllTasks(TaskStatus status, Pageable pageable);
    TaskResponse getTaskById(Long id);
    List<TaskResponse> getTasksByUserId(Long userId);
    TaskResponse updateTask(Long id, TaskUpdateRequest request);
    void deleteTask(Long id);
}
