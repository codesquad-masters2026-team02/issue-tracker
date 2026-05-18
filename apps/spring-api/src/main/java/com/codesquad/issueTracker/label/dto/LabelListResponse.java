package com.codesquad.issueTracker.label.dto;

import com.codesquad.issueTracker.label.Label;
import java.util.List;

public record LabelListResponse(
        List<LabelDetailResponse> labels
) {
    public static LabelListResponse from(List<Label> labels) {
        List<LabelDetailResponse> list = labels.stream()
                .map(LabelDetailResponse::from)
                .toList();

        return new LabelListResponse(list);
    }
}
