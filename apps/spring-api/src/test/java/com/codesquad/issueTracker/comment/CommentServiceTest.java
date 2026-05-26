package com.codesquad.issueTracker.comment;

import static com.codesquad.issueTracker.TestFixtures.issue;
import static com.codesquad.issueTracker.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codesquad.issueTracker.comment.dto.CommentListResponse;
import com.codesquad.issueTracker.comment.dto.CommentRequest;
import com.codesquad.issueTracker.comment.dto.CommentResponse;
import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.issue.Issue;
import com.codesquad.issueTracker.issue.IssueRepository;
import com.codesquad.issueTracker.issue.IssueStatus;
import com.codesquad.issueTracker.user.User;
import com.codesquad.issueTracker.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CommentServiceTest {

    @Autowired
    CommentService commentService;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    IssueRepository issueRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void createsAndListsCommentsForIssueWithUsernamesAndAscendingOrder() {
        User author = userRepository.save(user("comment-author"));
        Issue issue = issueRepository.save(issue(author.getId(), "commented issue", IssueStatus.OPEN, null));

        CommentResponse first = commentService.postComment(
                issue.getId(),
                author.getId(),
                new CommentRequest("first"),
                CommentType.ISSUE_BODY
        );
        CommentResponse second = commentService.postComment(
                issue.getId(),
                author.getId(),
                new CommentRequest("second"),
                CommentType.DISCUSSION
        );

        CommentListResponse response = commentService.getCommentListForIssue(issue.getId());

        assertThat(response.issueNumber()).isEqualTo(issue.getId());
        assertThat(response.comment_count()).isEqualTo(2);
        assertThat(response.comments())
                .extracting(CommentResponse::id)
                .containsExactly(first.id(), second.id());
        assertThat(response.comments())
                .extracting(CommentResponse::username)
                .containsOnly("comment-author");
    }

    @Test
    void creatingCommentForMissingIssueThrowsIssueNotFound() {
        User author = userRepository.save(user("missing-comment-author"));

        assertThatThrownBy(() -> commentService.postComment(
                999_999L,
                author.getId(),
                new CommentRequest("orphan"),
                CommentType.DISCUSSION
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ISSUE_NOT_FOUND);
    }

    @Test
    void deletingOwnCommentSucceeds() {
        User author = userRepository.save(user("delete-author"));
        Issue issue = issueRepository.save(issue(author.getId(), "delete issue", IssueStatus.OPEN, null));
        CommentResponse comment = commentService.postComment(
                issue.getId(),
                author.getId(),
                new CommentRequest("delete me"),
                CommentType.DISCUSSION
        );

        commentService.deleteCommentByCommentIds(comment.id(), author.getId());

        assertThat(commentRepository.findById(comment.id())).isEmpty();
    }

    @Test
    void deletingSomeoneElsesCommentThrowsUnauthorizedModification() {
        User author = userRepository.save(user("owner-user"));
        User other = userRepository.save(user("other-user"));
        Issue issue = issueRepository.save(issue(author.getId(), "protected issue", IssueStatus.OPEN, null));
        CommentResponse comment = commentService.postComment(
                issue.getId(),
                author.getId(),
                new CommentRequest("owned"),
                CommentType.DISCUSSION
        );

        assertThatThrownBy(() -> commentService.deleteCommentByCommentIds(comment.id(), other.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED_MODIFICATION);
    }

    @Test
    void deletingMissingCommentThrowsCommentNotFound() {
        User author = userRepository.save(user("missing-delete-author"));

        assertThatThrownBy(() -> commentService.deleteCommentByCommentIds(999_999L, author.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
    }
}
