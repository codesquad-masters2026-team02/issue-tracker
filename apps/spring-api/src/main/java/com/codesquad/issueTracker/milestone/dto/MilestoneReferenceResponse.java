package com.codesquad.issueTracker.milestone.dto;

import com.codesquad.issueTracker.milestone.Milestone;

public record MilestoneReferenceResponse(
        Long milestoneId,
        String title
) {
    public static MilestoneReferenceResponse from(Milestone milestone) {
        return new MilestoneReferenceResponse(milestone.getId(), milestone.getName());
    }
}
