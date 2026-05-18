package com.codesquad.issueTracker.user.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "유저 네임은 필수입니다")
        @Size(min=4, max=32, message = "올바르지 않은 유저이름 형식입니다")
        String username,
        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min=8, max = 64, message = "올바르지 않은 비밀번호 형식입니다")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                message = "올바르지 않은 비밀번호 형식입니다"
        )
        String password
){
}
