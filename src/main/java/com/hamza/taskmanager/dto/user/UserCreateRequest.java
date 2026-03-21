package com.hamza.taskmanager.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 20, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    private String email;

    @NotBlank
    @Size(min = 8,max = 100, message = "Password must have at least 8 characters")
    private String password;
}
