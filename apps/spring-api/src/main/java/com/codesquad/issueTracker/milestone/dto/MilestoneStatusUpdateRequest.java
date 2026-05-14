package com.codesquad.issueTracker.milestone.dto;

import com.codesquad.issueTracker.milestone.MilestoneStatus;
import jakarta.validation.constraints.NotNull;

public record MilestoneStatusUpdateRequest(
    @NotNull(message = "Status cannot be null")
    MilestoneStatus status
) {
}
