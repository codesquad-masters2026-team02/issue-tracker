package com.codesquad.issueTracker.milestone.dto;

import com.codesquad.issueTracker.milestone.Milestone;
import com.codesquad.issueTracker.milestone.MilestoneStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record MilestoneResponse(
        Long id,
        String name,
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy. MM. dd")
        LocalDate dueDate,
        MilestoneStatus status,
        Integer openIssueCount,
        Integer closedIssueCount
) {
    public MilestoneResponse (Milestone milestone){
        this(milestone.getId(), milestone.getName(), milestone.getDescription(), milestone.getDueDate(), milestone.getStatus(), milestone.getOpenIssueCount(), milestone.getClosedIssueCount());
    }
}
