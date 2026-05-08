package com.codesquad.issueTracker.issue.dto;

import com.codesquad.issueTracker.issue.Issue;
import com.codesquad.issueTracker.issue.IssueStatus;
import java.time.LocalDateTime;

public record IssueResponse(
        Long issueNumber,
        String title,
        IssueStatus status,
        LocalDateTime createdAt
) {
    public static IssueResponse from(Issue issue) {
        return new IssueResponse(issue.getIssueNumber(), issue.getTitle(), issue.getStatus(), issue.getCreatedAt());
    }
}
