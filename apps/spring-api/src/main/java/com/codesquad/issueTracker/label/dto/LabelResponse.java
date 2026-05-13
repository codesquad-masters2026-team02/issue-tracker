package com.codesquad.issueTracker.label.dto;

import com.codesquad.issueTracker.label.Label;
import com.codesquad.issueTracker.label.TextColor;

public record LabelResponse (
        Long labelId,
        String name,
        String description,
        String backgroundColor,
        TextColor textColor
) {
    public static LabelResponse from(Label label) {
        return new LabelResponse(label.getLabelId(), label.getName(), label.getDescription(),
                label.getBackgroundColor(), label.getTextColor());
    }

}
