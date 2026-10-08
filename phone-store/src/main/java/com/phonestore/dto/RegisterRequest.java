package com.phonestore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username không được để trống")
        @Size(min = 3, max = 50, message = "Username từ 3-50 ký tự")
        String username,

        @NotBlank(message = "Password không được để trống")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{6,}$",
                message = "Password tối thiểu 6 ký tự, gồm chữ thường, chữ hoa, số và ký tự đặc biệt")
        String password) {
}
