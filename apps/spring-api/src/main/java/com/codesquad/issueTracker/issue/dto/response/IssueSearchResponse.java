package com.codesquad.issueTracker.issue.dto.response;

import java.util.List;

public record IssueSearchResponse(
        long openIssueCount,
        long closedIssueCount,
        int pageNumber,
        int pageSize,
        long totalIssueCount,
        int totalPages,
        boolean first,
        boolean last,
        List<IssueSummaryResponse> issues
) {
    public static IssueSearchResponse from(long openIssueCount, long closedIssueCount, int pageNumber, int pageSize, long totalIssueCount, int totalPages, boolean
            first, boolean last ,List<IssueSummaryResponse> issues) {
        return new IssueSearchResponse(openIssueCount, closedIssueCount,pageNumber,pageSize,totalIssueCount,totalPages,first,last, issues);
    }
}
