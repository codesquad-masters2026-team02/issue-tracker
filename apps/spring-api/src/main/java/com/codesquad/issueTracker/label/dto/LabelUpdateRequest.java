package com.codesquad.issueTracker.label.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LabelUpdateRequest(
        @NotNull
        List<Long> labelIds
) {
}
