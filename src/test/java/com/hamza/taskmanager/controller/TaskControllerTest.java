package com.hamza.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.taskmanager.dto.task.TaskCreateRequest;
import com.hamza.taskmanager.entity.User;
import com.hamza.taskmanager.enums.TaskPriority;
import com.hamza.taskmanager.enums.TaskStatus;
import com.hamza.taskmanager.enums.UserRole;
import com.hamza.taskmanager.repository.TaskRepository;
import com.hamza.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp(){
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test //happy-path test 200
    void shouldCreateTaskSuccessfully() throws Exception{

        User savedUser = userRepository.save(
                User.builder()
                        .name("Hamza")
                        .email("hamza@example.com")
                        .password("secret")
                        .role(UserRole.USER)
                        .build()
        );

        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("Learn Spring Boot");
        request.setDescription("Finish Crud and understand architecture");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(3));
        request.setUserId(savedUser.getId());

        mockMvc.perform(post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.description").value("Finish Crud and understand architecture"))
                .andExpect(jsonPath("$.userId").value(savedUser.getId()));

    }

    @Test //validation failure test 400
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        User savedUser = userRepository.save(
                User.builder()
                        .name("Hamza")
                        .email("hamza@example.com")
                        .password("secret")
                        .role(UserRole.USER)
                        .build()
        );

        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle(" ");
        request.setDescription("Finish Crud and understand architecture");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(3));
        request.setUserId(savedUser.getId());

        mockMvc.perform(post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.title").value("Title is required"));
    }

    @Test //not-found failure test 404
    void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception{
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Task not found with id 999"));
    }

    @Test
    void shouldReturnAllTasks
}
