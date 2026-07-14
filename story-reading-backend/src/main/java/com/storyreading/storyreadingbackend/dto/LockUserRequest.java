package com.storyreading.storyreadingbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LockUserRequest {

    @NotBlank(message = "Lý do khóa tài khoản không được để trống")
    private String reason;
}