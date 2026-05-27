package com.codesquad.issueTracker.issue.dto.request;

import jakarta.validation.constraints.NotBlank;

public record IssueTitleUpdateRequest(
        @NotBlank String title
) {
}
