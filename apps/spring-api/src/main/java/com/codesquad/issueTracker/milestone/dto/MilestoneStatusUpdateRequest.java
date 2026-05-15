package com.codesquad.issueTracker.milestone.dto;

import com.codesquad.issueTracker.milestone.MilestoneStatus;
import jakarta.validation.constraints.NotNull;

public record MilestoneStatusUpdateRequest(
    @NotNull(message = "마일스톤 상태는 필수 항목입니다")
    MilestoneStatus status
) {
}
