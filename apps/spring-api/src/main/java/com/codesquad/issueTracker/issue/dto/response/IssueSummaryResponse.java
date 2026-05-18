package com.codesquad.issueTracker.issue.dto.response;

import com.codesquad.issueTracker.issue.Issue;
import com.codesquad.issueTracker.issue.IssueStatus;

import com.codesquad.issueTracker.label.dto.LabelSummaryResponse;
import com.codesquad.issueTracker.milestone.dto.MilestoneReferenceResponse;
import java.time.LocalDateTime;
import java.util.List;

public record IssueSummaryResponse(
        Long issueNumber,
        String title,
        IssueStatus status,
        LocalDateTime createdAt,
        List<LabelSummaryResponse> labels,
        MilestoneReferenceResponse milestone
) {
    public static IssueSummaryResponse from(Issue issue, List<LabelSummaryResponse> labels,
                                            MilestoneReferenceResponse milestone
    ) {
        return new IssueSummaryResponse(issue.getId(), issue.getTitle(), issue.getStatus(), issue.getCreatedAt(),
                labels, milestone);
    }
}
