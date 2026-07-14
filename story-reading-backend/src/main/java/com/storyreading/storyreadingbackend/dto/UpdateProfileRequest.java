package com.storyreading.storyreadingbackend.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateProfileRequest {

    @Email(message = "Email không đúng định dạng")
    private String email;

    /** URL ảnh đại diện nhập tay (chưa tích hợp upload trực tiếp). */
    private String avatarUrl;

    private LocalDate birthDate;
}
