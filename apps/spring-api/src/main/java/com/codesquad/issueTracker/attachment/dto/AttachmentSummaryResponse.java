package com.codesquad.issueTracker.attachment.dto;

import com.codesquad.issueTracker.attachment.Attachment;

public record AttachmentSummaryResponse(
        String attachmentId,
        String filename,
        String contentType,
        String publicUrl
) {
    public static AttachmentSummaryResponse from(Attachment attachment) {
        return new AttachmentSummaryResponse(
                attachment.getId().toString(),
                attachment.getFilename(),
                attachment.getContentType(),
                "/api/attachments/" + attachment.getId()
        );
    }
}
