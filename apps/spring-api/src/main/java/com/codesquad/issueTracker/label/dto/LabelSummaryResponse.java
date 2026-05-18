package com.codesquad.issueTracker.label.dto;

import com.codesquad.issueTracker.label.Label;
import com.codesquad.issueTracker.label.TextColor;

public record LabelSummaryResponse(
        Long labelId,
        String name,
        String backgroundColor,
        TextColor textColor
) {
    public static LabelSummaryResponse from(Label label) {
        return new LabelSummaryResponse(label.getId(), label.getName(), label.getBackgroundColor(), label.getTextColor());
    }
}
