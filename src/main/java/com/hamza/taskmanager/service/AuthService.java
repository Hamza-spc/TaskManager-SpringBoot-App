package com.hamza.taskmanager.service;

import com.hamza.taskmanager.dto.auth.AuthRequest;
import com.hamza.taskmanager.dto.auth.AuthResponse;

public interface AuthService {
    AuthResponse authenticate(AuthRequest request);
}
