package com.hamza.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.taskmanager.dto.task.TaskCreateRequest;
import com.hamza.taskmanager.dto.task.TaskUpdateRequest;
import com.hamza.taskmanager.entity.Task;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private User createUser(String name, String email, String password) {
        return userRepository.save(
                User.builder()
                        .name(name)
                        .email(email)
                        .password(password)
                        .role(UserRole.USER)
                        .build()
        );
    }

    private Task createTask(User user, String title, String description) {
        return taskRepository.save(
                Task.builder()
                        .title(title)
                        .description(description)
                        .status(TaskStatus.IN_PROGRESS)
                        .priority(TaskPriority.HIGH)
                        .dueDate(LocalDate.now().plusDays(3))
                        .user(user)
                        .build()
        );
    }

    @Test //happy-path test 200
    void shouldCreateTaskSuccessfully() throws Exception{
        User savedUser = createUser("Hamza", "hamza@example.com", "secret");

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
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Task created successfully"))
                .andExpect(jsonPath("$.data.title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.data.description").value("Finish Crud and understand architecture"))
                .andExpect(jsonPath("$.data.userId").value(savedUser.getId()));

    }

    @Test //validation failure test 400
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        User savedUser = createUser("Hamza", "hamza@example.com", "secret");

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
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").value("Title is required"));
    }

    @Test //not-found failure test 404
    void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception{
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id 999"));
    }

    @Test
    void shouldReturnAllTasks() throws Exception{
        User savedUser = createUser("Hamza", "hamza@example.com", "secret");
        createTask(savedUser, "Learn Spring Boot", "Project Based Learning");

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Tasks fetched successfully"))
                .andExpect(jsonPath("$.data.content.size()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.data.content[0].description").value("Project Based Learning"))
                .andExpect(jsonPath("$.data.content[0].userId").value(savedUser.getId()));
    }

    @Test
    void shouldReturnTaskById() throws Exception{
        User savedUser = createUser("Hamza", "hamza@example.com", "secret");
        Task savedTask = createTask(savedUser, "Learn Spring Boot", "Project Based Learning");

        mockMvc.perform(get("/api/tasks/"+savedTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Task fetched successfully"))
                .andExpect(jsonPath("$.data.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.data.title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.data.description").value("Project Based Learning"))
                .andExpect(jsonPath("$.data.userId").value(savedUser.getId()));

    }

    @Test
    void shouldReturnTasksByUserId() throws Exception{
        User savedUser1 = createUser("Hamza1", "hamza1@example.com", "secret1");
        User savedUser2 = createUser("Hamza2", "hamza2@example.com", "secret2");
        createTask(savedUser1, "Learn Spring Boot 1", "Project Based Learning 1");
        createTask(savedUser2, "Master Spring Boot CRUD", "Project Based Learning 2");

        mockMvc.perform(get("/api/tasks/user/" + savedUser1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User tasks fetched successfully"))
                .andExpect(jsonPath("$.data.size()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Learn Spring Boot 1"))
                .andExpect(jsonPath("$.data[0].description").value("Project Based Learning 1"))
                .andExpect(jsonPath("$.data[0].userId").value(savedUser1.getId()));
    }

    @Test
    void shouldUpdateTaskSuccessfully() throws Exception{
        User savedUser1 = createUser("Hamza1", "hamza1@example.com", "secret1");
        Task savedTask1 = createTask(savedUser1, "Learn Spring Boot", "Project Based Learning");

        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setTitle("Master Spring Boot");
        request.setDescription("Practicing MockMvc testing");
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(7));

        mockMvc.perform(put("/api/tasks/" + savedTask1.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Task updated successfully"))
                .andExpect(jsonPath("$.data.title").value("Master Spring Boot"))
                .andExpect(jsonPath("$.data.description").value("Practicing MockMvc testing"))
                .andExpect(jsonPath("$.data.userId").value(savedUser1.getId()));
    }

    @Test
    void shouldDeleteTaskSuccessfully() throws Exception{
        User savedUser1 = createUser("Hamza1", "hamza1@example.com", "secret1");
        Task savedTask1 = createTask(savedUser1, "Learn Spring Boot", "Project Based Learning");

        mockMvc.perform(delete("/api/tasks/"+savedTask1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Task deleted successfully"));
        assertFalse(taskRepository.findById(savedTask1.getId()).isPresent());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingTask() throws Exception {

        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setTitle("Master Spring Boot");
        request.setDescription("Practicing MockMvc testing");
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(7));

        mockMvc.perform(put("/api/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id 999"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id 999"));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingTaskWithBlankTitle() throws Exception {
        User savedUser1 = createUser("Hamza1", "hamza1@example.com", "secret1");
        Task savedTask1 = createTask(savedUser1, "Learn Spring Boot", "Project Based Learning");

        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setTitle(" ");
        request.setDescription("Practicing MockMvc testing");
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(7));

        mockMvc.perform(put("/api/tasks/" + savedTask1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").value("Title is required"));
    }
}
