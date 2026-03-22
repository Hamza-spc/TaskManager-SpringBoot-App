package com.hamza.taskmanager.service.impl;

import com.hamza.taskmanager.dto.task.TaskCreateRequest;
import com.hamza.taskmanager.dto.task.TaskResponse;
import com.hamza.taskmanager.dto.task.TaskUpdateRequest;
import com.hamza.taskmanager.entity.Task;
import com.hamza.taskmanager.entity.User;
import com.hamza.taskmanager.enums.TaskPriority;
import com.hamza.taskmanager.enums.TaskStatus;
import com.hamza.taskmanager.exception.TaskNotFoundException;
import com.hamza.taskmanager.exception.UserNotFoundException;
import com.hamza.taskmanager.repository.TaskRepository;
import com.hamza.taskmanager.repository.UserRepository;
import com.hamza.taskmanager.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TaskResponse createTask(TaskCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + request.getUserId()));

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .user(user)
                .build();

        Task savedTask = taskRepository.save(task);
        return mapToResponse(savedTask);
    }

    @Override
    public Page<TaskResponse> getAllTasks(TaskStatus status, Pageable pageable) {
        if (status != null) {
            return taskRepository.findByStatus(status, pageable)
                    .map(this::mapToResponse);
        }

        return taskRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id " + id));
        return mapToResponse(task);
    }

    @Override
    public List<TaskResponse> getTasksByUserId(Long userId) {
        return taskRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id " + id));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        task.setDueDate(request.getDueDate());

        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id " + id));

        taskRepository.delete(task);
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .userId(task.getUser().getId())
                .build();
    }
}
