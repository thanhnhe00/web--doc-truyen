package com.storyreading.storyreadingbackend.controller;

import com.storyreading.storyreadingbackend.dto.AuthResponse;
import com.storyreading.storyreadingbackend.dto.LoginRequest;
import com.storyreading.storyreadingbackend.dto.RegisterRequest;
import com.storyreading.storyreadingbackend.entity.User;
import com.storyreading.storyreadingbackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User newUser = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Đăng ký thành công. Tài khoản: " + newUser.getUsername());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}