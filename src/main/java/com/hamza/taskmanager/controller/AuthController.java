package com.hamza.taskmanager.controller;

import com.hamza.taskmanager.dto.auth.AuthRequest;
import com.hamza.taskmanager.dto.auth.AuthResponse;
import com.hamza.taskmanager.dto.common.ApiSuccessResponse;
import com.hamza.taskmanager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(ApiSuccessResponse.<AuthResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Login successful")
                .data(authService.authenticate(request))
                .build());
    }
}
