package com.codesquad.issueTracker.milestone.dto;

import com.codesquad.issueTracker.milestone.Milestone;
import com.codesquad.issueTracker.milestone.MilestoneStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record MilestoneRequest (
        @NotBlank String name,
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy. MM. dd")
        LocalDate dueDate
){
    public Milestone toEntity(){
        return new Milestone(null, name, dueDate,description,0,0, MilestoneStatus.OPEN,false);
    }
}