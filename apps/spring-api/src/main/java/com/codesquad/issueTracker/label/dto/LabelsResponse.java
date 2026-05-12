package com.codesquad.issueTracker.label.dto;

import com.codesquad.issueTracker.label.Label;
import java.util.List;

public record LabelsResponse (
        List<LabelResponse> labels
) {
    public static LabelsResponse from(List<Label> labels) {
        List<LabelResponse> list = labels.stream()
                .map(LabelResponse::from)
                .toList();

        return new LabelsResponse(list);
    }
}
