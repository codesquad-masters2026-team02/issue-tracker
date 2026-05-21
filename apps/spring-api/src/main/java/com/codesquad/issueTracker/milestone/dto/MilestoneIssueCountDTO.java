package com.codesquad.issueTracker.milestone.dto;

public record MilestoneIssueCountDTO(
        Long milestoneId,
        Long openIssueCount,
        Long closedIssueCount
) {
}
