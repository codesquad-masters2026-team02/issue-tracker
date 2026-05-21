package com.codesquad.issueTracker.issue.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssigneeUpdateRequest(
        @NotNull
        List<Long> userIds
) {
}
