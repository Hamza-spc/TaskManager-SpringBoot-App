package com.hamza.taskmanager.repository;

import com.hamza.taskmanager.entity.Task;
import com.hamza.taskmanager.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Long> {
    List<Task> findByUserId(Long userId);
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
}
