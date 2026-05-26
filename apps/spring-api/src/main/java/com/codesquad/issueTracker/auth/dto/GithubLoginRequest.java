package com.codesquad.issueTracker.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GithubLoginRequest(
        @NotBlank String code
) {
}
