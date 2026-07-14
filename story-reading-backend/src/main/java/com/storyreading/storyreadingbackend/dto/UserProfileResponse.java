package com.storyreading.storyreadingbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String username;
    private String email;
    private LocalDate birthDate;
    private String avatarUrl;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}
