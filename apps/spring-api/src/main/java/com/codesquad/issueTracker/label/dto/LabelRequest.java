package com.codesquad.issueTracker.label.dto;


import com.codesquad.issueTracker.label.Label;
import com.codesquad.issueTracker.label.TextColor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record LabelRequest(
        @NotBlank String name,
        String description,

        @NotBlank
        @Pattern(regexp = "^#[A-Fa-f0-9]{6}$", message = "유효한 hex 색상 코드여야 합니다 (예: #FF5733)")
        String backgroundColor,

        @NotNull TextColor textColor
) {
    public Label toEntity() {
        return new Label(null, name, description, backgroundColor, textColor);
    }
}
