package com.codesquad.issueTracker.milestone.dto;

import com.codesquad.issueTracker.milestone.Milestone;

import java.util.List;

public record MilestoneListResponse(
        Integer milestoneCount,
        Integer openMilestoneCount,
        Integer closedMilestoneCount,
        List<MilestoneResponse> milestones
) {
    public MilestoneListResponse(List<MilestoneResponse> milestoneList,int openMilestoneCount, int closedMilestoneCount){
        this( (openMilestoneCount + closedMilestoneCount),
                openMilestoneCount,
                closedMilestoneCount,
                milestoneList);
    }
}
