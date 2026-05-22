package com.codesquad.issueTracker.milestone.dto;

import jakarta.validation.constraints.NotNull;

public record MilestoneSidebarUpdateRequest (
        @NotNull
        Long milestoneId
){
}
