package com.codesquad.issueTracker.comment;

import com.codesquad.issueTracker.comment.dto.CommentRequest;
import com.codesquad.issueTracker.comment.dto.CommentResponse;
import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.issue.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepo;
    private final IssueService issueService;

    public CommentResponse postComment(Long issueId, CommentRequest request, CommentType type){
        if(!issueService.existsById(issueId)){
            throw new BusinessException(ErrorCode.ISSUE_NOT_FOUND);
        }
        else{
            Comment newComment = request.toEntity(issueId, type);
            Comment savedComment = commentRepo.save(newComment);
            return new CommentResponse(savedComment);
        }
    }
}
