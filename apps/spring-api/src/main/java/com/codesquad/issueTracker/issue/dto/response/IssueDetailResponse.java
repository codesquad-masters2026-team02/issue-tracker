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
        String authorUsername,
        List<LabelSummaryResponse> labels,
        MilestoneSummaryResponse milestone,
        List<AssigneeSummaryResponse> assignees
) {
    public static IssueDetailResponse from(Issue issue, List<LabelSummaryResponse> labels,
                                           MilestoneSummaryResponse milestone, List<AssigneeSummaryResponse> assignees
    ) {
        return from(issue, "알 수 없음", labels, milestone, assignees);
    }

    public static IssueDetailResponse from(Issue issue, String authorUsername, List<LabelSummaryResponse> labels,
                                           MilestoneSummaryResponse milestone, List<AssigneeSummaryResponse> assignees
    ) {
        return new IssueDetailResponse(issue.getId(), issue.getTitle(), issue.getStatus(), issue.getCreatedAt(),
                authorUsername, labels, milestone, assignees);
    }
}
