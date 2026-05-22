package com.codesquad.issueTracker.attachment.dto;

public record PresignResponse(
        String uploadUrl,
        String attachmentId,
        String publicUrl
) {
}
