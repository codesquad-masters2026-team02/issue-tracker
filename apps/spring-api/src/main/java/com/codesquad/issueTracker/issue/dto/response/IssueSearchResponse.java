package com.codesquad.issueTracker.issue.dto.response;

import java.util.List;

public record IssueSearchResponse(
        long openIssueCount,
        long closedIssueCount,
        List<IssueSummaryResponse> issues
) {
    public static IssueSearchResponse from(long openIssueCount, long closedIssueCount, List<IssueSummaryResponse> issues) {
        return new IssueSearchResponse(openIssueCount, closedIssueCount, issues);
    }
}
