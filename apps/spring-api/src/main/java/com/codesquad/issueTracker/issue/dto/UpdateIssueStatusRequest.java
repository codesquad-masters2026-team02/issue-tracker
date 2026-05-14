package com.codesquad.issueTracker.issue.dto;

import com.codesquad.issueTracker.issue.IssueStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateIssueStatusRequest(
        @NotNull IssueStatus status
) {

}
