package com.hamza.taskmanager.service;

import com.hamza.taskmanager.dto.task.TaskCreateRequest;
import com.hamza.taskmanager.dto.task.TaskResponse;
import com.hamza.taskmanager.dto.task.TaskUpdateRequest;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(TaskCreateRequest request);
    List<TaskResponse> getAllTasks();
    TaskResponse getTaskById(Long id);
    List<TaskResponse> getTasksByUserId(Long userId);
    TaskResponse updateTask(Long id, TaskUpdateRequest request);
    void deleteTask(Long id);
}