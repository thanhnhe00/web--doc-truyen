package com.storyreading.storyreadingbackend.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Tên tài khoản không được để trống")
    @Size(min = 4, max = 50, message = "Tên tài khoản phải từ 4 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email sai định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
    private LocalDate birthDate;

    @NotBlank(message = "Vui lòng chọn loại tài khoản")
    @Pattern(regexp = "READER|CREATOR", message = "Loại tài khoản chỉ có thể là READER hoặc CREATOR")
    private String role;
}