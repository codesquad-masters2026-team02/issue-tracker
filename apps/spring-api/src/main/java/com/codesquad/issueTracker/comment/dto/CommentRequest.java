package com.codesquad.issueTracker.comment.dto;

import com.codesquad.issueTracker.comment.Comment;
import com.codesquad.issueTracker.comment.CommentType;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.util.List;
import java.util.UUID;

public record CommentRequest(
        String content,
        List<UUID> attachmentIds
) {
    public CommentRequest(String content) {
        this(content, List.of());
    }

    public List<UUID> attachmentIds() {
        return attachmentIds != null ? attachmentIds : List.of();
    }

    public Comment toEntity(Long issueId, Long userId, CommentType type) {
        return new Comment(null, content, type.name(), null, null, null, userId, AggregateReference.to(issueId));
    }
}
