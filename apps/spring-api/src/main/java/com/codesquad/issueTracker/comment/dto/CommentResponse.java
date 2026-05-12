package com.codesquad.issueTracker.comment.dto;

import com.codesquad.issueTracker.comment.Comment;

import java.time.LocalDateTime;

public record CommentResponse (
        Long id,
        String type,
        String content,
        LocalDateTime created_at
){

    public CommentResponse(Comment comment){
        this(comment.getId(),comment.getType(),comment.getContent(),comment.getCreatedAt());
    }
}