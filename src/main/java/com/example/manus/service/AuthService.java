package com.example.manus.service;

import com.example.manus.dto.LoginRequest;
import com.example.manus.dto.LoginResponse;
import com.example.manus.dto.RegisterRequest;
import com.example.manus.persistence.entity.User;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout();

    User register(RegisterRequest request);

    User getCurrentUser();
}