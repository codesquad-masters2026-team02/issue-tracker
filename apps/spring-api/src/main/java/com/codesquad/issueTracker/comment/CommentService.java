package com.codesquad.issueTracker.comment;

import com.codesquad.issueTracker.comment.dto.CommentListResponse;
import com.codesquad.issueTracker.comment.dto.CommentRequest;
import com.codesquad.issueTracker.comment.dto.CommentResponse;
import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.issue.IssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepo;
    private final IssueRepository issueRepository;

    public CommentResponse postComment(Long issueId, CommentRequest request, CommentType type){
        if(!issueRepository.existsById(issueId)){
            throw new BusinessException(ErrorCode.ISSUE_NOT_FOUND);
        }
        else{
            Comment newComment = request.toEntity(issueId, type);
            Comment savedComment = commentRepo.save(newComment);
            return new CommentResponse(savedComment);
        }
    }

    public CommentListResponse getCommentListForIssue(Long issueId){
        if(!issueRepository.existsById(issueId)){
            throw new BusinessException(ErrorCode.ISSUE_NOT_FOUND);
        }
        else{
            List<Comment> comments = commentRepo.findAllByIssueIdOrderByCreatedAtAsc(issueId);
            List<CommentResponse> commentResponses = comments.stream().map(CommentResponse::new).toList();
            return new CommentListResponse(issueId, commentResponses);
        }
    }

    public void deleteCommentByCommentIds(Long commentId, Long tokenUserId){

        int deletedRows = commentRepo.deleteCommentByIds(commentId, tokenUserId);

        if(deletedRows == 0){
                if(!commentRepo.existsById(commentId)){
                    throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
                }
                else{
                    throw new BusinessException(ErrorCode.UNAUTHORIZED_MODIFICATION);
                }
        }
    }
}
