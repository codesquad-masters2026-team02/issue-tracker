package com.codesquad.issueTracker.issue.dto.response;

import com.codesquad.issueTracker.issue.Issue;
import java.util.List;

public record FilteredIssuesResponse(
        long openIssueCount,
        long closedIssueCount,
        List<IssueResponse> issues
) {
    public static FilteredIssuesResponse from(
            long openIssueCount, long closedIssueCount,
            List<Issue> issues
    ) {
        List<IssueResponse> list = issues.stream()
                .map(IssueResponse::from)
                .toList();

        return new FilteredIssuesResponse(openIssueCount, closedIssueCount, list);
    }
}
