package com.hamza.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.taskmanager.dto.user.UserCreateRequest;
import com.hamza.taskmanager.dto.user.UserUpdateRequest;
import com.hamza.taskmanager.entity.User;
import com.hamza.taskmanager.repository.TaskRepository;
import com.hamza.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

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

    private User createUser(String name, String email, String password){
        return userRepository.save(
                User.builder()
                        .name(name)
                        .email(email)
                        .password(password)
                        .build()
        );
    }

    @Test
    void shouldCreateUserSuccessfully() throws Exception{
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Hamza");
        request.setEmail("hamza@email.com");
        request.setPassword("password");

        mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hamza"))
                .andExpect(jsonPath("$.email").value("hamza@email.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception{
        createUser("Hamza","hamza@email.com","password");
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Hamza");
        request.setEmail("hamza@email.com");
        request.setPassword("password");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already exists: " + request.getEmail()));
    }

    @Test
    void shouldReturnAllUsers() throws Exception{
        createUser("Hamza1","hamza1@email.com","password1");
        createUser("Hamza2","hamza2@email.com","password2");

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Hamza1"))
                .andExpect(jsonPath("$[0].email").value("hamza1@email.com"))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[1].name").value("Hamza2"))
                .andExpect(jsonPath("$[1].email").value("hamza2@email.com"))
                .andExpect(jsonPath("$[1].password").doesNotExist());
    }

    @Test
    void shouldReturnUserById() throws Exception {
        User savedUser = createUser("Hamza", "hamza@email.com", "password");

        mockMvc.perform(get("/api/users/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("Hamza"))
                .andExpect(jsonPath("$.email").value("hamza@email.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with id 999"));
    }

    @Test
    void shouldUpdateUserSuccessfully() throws Exception {
        User savedUser = createUser("Hamza", "hamza@email.com", "password");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Hamza Updated");
        request.setEmail("hamza.updated@email.com");
        request.setPassword("newpassword");

        mockMvc.perform(put("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("Hamza Updated"))
                .andExpect(jsonPath("$.email").value("hamza.updated@email.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturnConflictWhenUpdatingUserWithExistingEmail() throws Exception {
        User firstUser = createUser("Hamza", "hamza@email.com", "password");
        createUser("Omar", "omar@email.com", "password2");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Hamza Updated");
        request.setEmail("omar@email.com");
        request.setPassword("newpassword");

        mockMvc.perform(put("/api/users/" + firstUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already exists: omar@email.com"));
    }

    @Test
    void shouldDeleteUserSuccessfully() throws Exception {
        User savedUser = createUser("Hamza", "hamza@email.com", "password");

        mockMvc.perform(delete("/api/users/" + savedUser.getId()))
                .andExpect(status().isOk());

        assertFalse(userRepository.findById(savedUser.getId()).isPresent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingUser() throws Exception {
        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with id 999"));
    }
}
