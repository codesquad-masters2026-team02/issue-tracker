package com.codesquad.issueTracker.issue.dto.response;

import com.codesquad.issueTracker.issue.Issue;
import com.codesquad.issueTracker.issue.IssueStatus;
import com.codesquad.issueTracker.label.dto.LabelSummaryResponse;
import com.codesquad.issueTracker.milestone.dto.MilestoneSummaryResponse;
import java.time.LocalDateTime;
import java.util.List;

public record IssueDetailResponse(
        Long issueNumber,
        String title,
        IssueStatus status,
        LocalDateTime createdAt,
        List<LabelSummaryResponse> labels,
        MilestoneSummaryResponse milestone
) {
    public static IssueDetailResponse from(Issue issue, List<LabelSummaryResponse> labels,
                                           MilestoneSummaryResponse milestone
    ) {
        return new IssueDetailResponse(issue.getId(), issue.getTitle(), issue.getStatus(), issue.getCreatedAt(),
                labels, milestone);
    }
}
