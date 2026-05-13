package com.codesquad.issueTracker.comment.dto;

import com.codesquad.issueTracker.comment.Comment;

import java.util.List;

public record CommentListResponse(
        Long issueNumber,
        Integer comment_count,
        List<CommentResponse> comments
) {
    public CommentListResponse(Long id, List<CommentResponse> comms){
        this(id, comms.size(), comms);
    }
}