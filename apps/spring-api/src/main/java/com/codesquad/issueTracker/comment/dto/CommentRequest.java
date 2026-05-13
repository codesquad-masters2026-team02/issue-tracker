package com.codesquad.issueTracker.comment.dto;

import com.codesquad.issueTracker.comment.Comment;
import com.codesquad.issueTracker.comment.CommentType;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

public record CommentRequest (
        String content
){
    public Comment toEntity(Long issueId, CommentType type){
        return new Comment(null,content,type.name(),null,null,null,null, AggregateReference.to(issueId));
    }
}
