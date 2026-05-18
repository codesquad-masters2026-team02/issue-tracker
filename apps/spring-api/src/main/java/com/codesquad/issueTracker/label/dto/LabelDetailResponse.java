package com.codesquad.issueTracker.label.dto;

import com.codesquad.issueTracker.label.Label;
import com.codesquad.issueTracker.label.TextColor;

public record LabelDetailResponse(
        Long labelId,
        String name,
        String description,
        String backgroundColor,
        TextColor textColor
) {
    public static LabelDetailResponse from(Label label) {
        return new LabelDetailResponse(label.getId(), label.getName(), label.getDescription(),
                label.getBackgroundColor(), label.getTextColor());
    }

}
