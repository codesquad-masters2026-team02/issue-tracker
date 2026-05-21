package com.codesquad.issueTracker.attachment.dto;

import jakarta.validation.constraints.NotNull;

public record PresignRequest(
        @NotNull String filename,
        @NotNull String contentType,
        @NotNull Long size
) {
}
