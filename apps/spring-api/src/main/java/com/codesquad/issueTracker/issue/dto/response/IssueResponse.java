package com.codesquad.issueTracker.issue.dto.response;

import com.codesquad.issueTracker.issue.Issue;
import com.codesquad.issueTracker.issue.IssueStatus;
import com.codesquad.issueTracker.milestone.dto.MilestoneResponse;

import java.time.LocalDateTime;

public record IssueResponse(
        Long issueNumber,
        String title,
        IssueStatus status,
        LocalDateTime createdAt,
        Long milestoneId
        ) {
    public static IssueResponse from(Issue issue) {
        return new IssueResponse(issue.getIssueNumber(), issue.getTitle(), issue.getStatus(), issue.getCreatedAt(), issue.getMilestoneId());
    }
}
