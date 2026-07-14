package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.dto.AuthResponse;
import com.storyreading.storyreadingbackend.dto.LoginRequest;
import com.storyreading.storyreadingbackend.dto.RegisterRequest;
import com.storyreading.storyreadingbackend.entity.User;
import com.storyreading.storyreadingbackend.entity.enums.UserRole;
import com.storyreading.storyreadingbackend.entity.enums.UserStatus;
import com.storyreading.storyreadingbackend.repository.UserRepository;
import com.storyreading.storyreadingbackend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên tài khoản đã được sử dụng");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã được sử dụng");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .birthDate(request.getBirthDate())
                .role(UserRole.valueOf(request.getRole()))
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        // Luồng ngoại lệ 3a: sai tài khoản hoặc mật khẩu (dùng chung 1 thông báo)
        User user = userRepository.findByUsernameOrEmail(
                        request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Tài khoản hoặc mật khẩu không chính xác"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Tài khoản hoặc mật khẩu không chính xác");
        }

        // Luồng ngoại lệ 4a: tài khoản bị khóa
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}