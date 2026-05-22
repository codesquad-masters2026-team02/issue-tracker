package com.codesquad.issueTracker.milestone.dto;

import com.codesquad.issueTracker.milestone.Milestone;

public record MilestoneSummaryResponse(
        Long id,
        String name,
        Long openIssueCount,
        Long closedIssueCount
) {
    public static MilestoneSummaryResponse from(Milestone milestone, Long openCount, Long closedCount) {
        return new MilestoneSummaryResponse(milestone.getId(), milestone.getName(), openCount,closedCount);
    }

}
