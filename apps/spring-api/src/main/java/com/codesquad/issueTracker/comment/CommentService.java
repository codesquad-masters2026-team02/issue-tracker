package com.codesquad.issueTracker.comment;

import com.codesquad.issueTracker.attachment.AttachmentService;
import com.codesquad.issueTracker.attachment.dto.AttachmentSummaryResponse;
import com.codesquad.issueTracker.comment.dto.CommentListResponse;
import com.codesquad.issueTracker.comment.dto.CommentRequest;
import com.codesquad.issueTracker.comment.dto.CommentResponse;
import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.issue.IssueRepository;
import com.codesquad.issueTracker.user.User;
import com.codesquad.issueTracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepo;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final AttachmentService attachmentService;

    public CommentResponse postComment(Long issueId, Long userId, CommentRequest request, CommentType type) {
        if (!issueRepository.existsById(issueId)) {
            throw new BusinessException(ErrorCode.ISSUE_NOT_FOUND);
        }
        Comment newComment = request.toEntity(issueId, userId, type);
        Comment savedComment = commentRepo.save(newComment);

        attachmentService.commitToComment(request.attachmentIds(), userId, savedComment.getId());

        List<AttachmentSummaryResponse> attachments = attachmentService
                .getSummariesByCommentIds(List.of(savedComment.getId()))
                .getOrDefault(savedComment.getId(), List.of());

        return new CommentResponse(savedComment, findUsername(userId), attachments);
    }

    public CommentListResponse getCommentListForIssue(Long issueId) {
        if (!issueRepository.existsById(issueId)) {
            throw new BusinessException(ErrorCode.ISSUE_NOT_FOUND);
        }
        List<Comment> comments = commentRepo.findAllByIssueIdOrderByCreatedAtAsc(issueId);
        Map<Long, User> userMap = findUserMapByComments(comments);

        List<Long> commentIds = comments.stream().map(Comment::getId).toList();
        Map<Long, List<AttachmentSummaryResponse>> attachmentMap =
                attachmentService.getSummariesByCommentIds(commentIds);

        List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> new CommentResponse(
                        comment,
                        usernameOf(userMap.get(comment.getUserId())),
                        attachmentMap.getOrDefault(comment.getId(), List.of())
                ))
                .toList();
        return new CommentListResponse(issueId, commentResponses);
    }

    public void deleteCommentByCommentIds(Long commentId, Long tokenUserId) {
        int deletedRows = commentRepo.deleteCommentByIds(commentId, tokenUserId);

        if (deletedRows == 0) {
            if (!commentRepo.existsById(commentId)) {
                throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
            } else {
                throw new BusinessException(ErrorCode.UNAUTHORIZED_MODIFICATION);
            }
        }
    }

    private Map<Long, User> findUserMapByComments(List<Comment> comments) {
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (userIds.isEmpty()) {
            return Map.of();
        }

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private String findUsername(Long userId) {
        if (userId == null) {
            return "알 수 없음";
        }

        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("알 수 없음");
    }

    private String usernameOf(User user) {
        return user == null ? "알 수 없음" : user.getUsername();
    }
}
