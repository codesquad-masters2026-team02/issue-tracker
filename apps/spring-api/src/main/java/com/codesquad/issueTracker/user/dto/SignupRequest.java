package com.codesquad.issueTracker.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest (
    @NotBlank(message = "유저 네임은 필수입니다")
    @Size(min=4, max=32, message = "유저네임은 최소 4글자 최대 32글자로 설정해주세요")
    String username,
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min=8, max = 64, message = "비밀번호는 최소 8글자 이상 최대 64글자 이하로 설정해주세요")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "비밀번호는 숫자, 영문 소문자, 영문 대문자, 특수문자 최소 하나씩이 필요합니다"
    )
    String password
){
}
