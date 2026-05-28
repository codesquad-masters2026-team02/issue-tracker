package com.codesquad.issueTracker.issue.dto.response;

import com.codesquad.issueTracker.issue.Issue;
import com.codesquad.issueTracker.issue.IssueStatus;

import com.codesquad.issueTracker.label.dto.LabelSummaryResponse;
import com.codesquad.issueTracker.milestone.dto.MilestoneReferenceResponse;
import com.codesquad.issueTracker.user.dto.UserInfoResponse;
import java.time.LocalDateTime;
import java.util.List;

public record IssueSummaryResponse(
        Long issueNumber,
        String title,
        IssueStatus status,
        String author,
        LocalDateTime createdAt,
        List<LabelSummaryResponse> labels,
        MilestoneReferenceResponse milestone,
        List<UserInfoResponse> assignees
) {
    public static IssueSummaryResponse from(
            Issue issue, String authorName, List<LabelSummaryResponse> labels,
            MilestoneReferenceResponse milestone, List<UserInfoResponse> assignees
    ) {
        return new IssueSummaryResponse(issue.getId(), issue.getTitle(), issue.getStatus(), authorName,
                issue.getCreatedAt(), labels, milestone, assignees);
    }
}
