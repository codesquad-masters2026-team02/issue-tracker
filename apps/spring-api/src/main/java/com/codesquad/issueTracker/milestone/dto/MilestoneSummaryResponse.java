package com.codesquad.issueTracker.milestone.dto;

import com.codesquad.issueTracker.milestone.Milestone;

public record MilestoneSummaryResponse(
        Long id,
        String name,
        Integer openIssueCount,
        Integer closedIssueCount
) {
    public static MilestoneSummaryResponse from(Milestone milestone) {
        return new MilestoneSummaryResponse(milestone.getId(), milestone.getName(), milestone.getOpenIssueCount(),
                milestone.getClosedIssueCount());
    }

}
