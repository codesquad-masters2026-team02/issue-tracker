package com.codesquad.issueTracker.comment.dto;

import com.codesquad.issueTracker.attachment.dto.AttachmentSummaryResponse;
import com.codesquad.issueTracker.comment.Comment;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse (
        Long id,
        String type,
        String content,
        LocalDateTime created_at,
        String username,
        List<AttachmentSummaryResponse> attachments
){

    public CommentResponse(Comment comment, String username, List<AttachmentSummaryResponse> attachments){
        this(comment.getId(),comment.getType(),comment.getContent(),comment.getCreatedAt(), username,attachments);
    }
}
