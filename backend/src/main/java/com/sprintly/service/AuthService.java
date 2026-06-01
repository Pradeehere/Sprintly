package com.sprintly.service;

import com.sprintly.dto.request.LoginRequest;
import com.sprintly.dto.request.RefreshTokenRequest;
import com.sprintly.dto.request.RegisterRequest;
import com.sprintly.dto.response.AuthResponse;
import com.sprintly.dto.response.UserResponse;

import java.util.UUID;

public interface AuthService {
    UserResponse register(RegisterRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse refresh(RefreshTokenRequest refreshRequest);
    void logout(UUID userId);
}
