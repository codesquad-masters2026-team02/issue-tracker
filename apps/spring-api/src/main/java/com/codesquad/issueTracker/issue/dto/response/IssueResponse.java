package com.codesquad.issueTracker.issue.dto.response;

import com.codesquad.issueTracker.issue.Issue;
import com.codesquad.issueTracker.issue.IssueStatus;

import com.codesquad.issueTracker.label.dto.LabelSummaryResponse;
import java.time.LocalDateTime;
import java.util.List;

public record IssueResponse(
        Long issueNumber,
        String title,
        IssueStatus status,
        LocalDateTime createdAt,
        Long milestoneId,
        List<LabelSummaryResponse> labels
) {
    public static IssueResponse from(Issue issue, List<LabelSummaryResponse> labels) {
        return new IssueResponse(issue.getId(), issue.getTitle(), issue.getStatus(), issue.getCreatedAt(),
                issue.getMilestoneId(), labels);
    }
}
