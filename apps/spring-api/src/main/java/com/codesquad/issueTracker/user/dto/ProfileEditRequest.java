package com.codesquad.issueTracker.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileEditRequest(
        @NotBlank String imageUrl
) {
}
