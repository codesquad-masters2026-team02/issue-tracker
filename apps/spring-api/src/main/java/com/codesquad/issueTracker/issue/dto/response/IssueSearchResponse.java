package com.codesquad.issueTracker.issue.dto.response;

import com.codesquad.issueTracker.issue.Issue;
import java.util.List;

public record IssueSearchResponse(
        long openIssueCount,
        long closedIssueCount,
        List<IssueResponse> issues
) {
    public static IssueSearchResponse from(
            long openIssueCount, long closedIssueCount,
            List<Issue> issues
    ) {
        List<IssueResponse> list = issues.stream()
                .map(IssueResponse::from)
                .toList();

        return new IssueSearchResponse(openIssueCount, closedIssueCount, list);
    }
}
