package com.codesquad.issueTracker.comment;

import static com.codesquad.issueTracker.TestFixtures.issue;
import static com.codesquad.issueTracker.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;

import com.codesquad.issueTracker.issue.Issue;
import com.codesquad.issueTracker.issue.IssueRepository;
import com.codesquad.issueTracker.issue.IssueStatus;
import com.codesquad.issueTracker.user.User;
import com.codesquad.issueTracker.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CommentRepositoryTest {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    IssueRepository issueRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void findsCommentsByIssueOrderedByCreatedAtAscending() {
        User author = userRepository.save(user("comment-repo-author"));
        Issue issue = issueRepository.save(issue(author.getId(), "comment repo issue", IssueStatus.OPEN, null));
        Long laterId = insertComment("later", issue.getId(), author.getId(), LocalDateTime.of(2026, 5, 2, 10, 0));
        Long earlierId = insertComment("earlier", issue.getId(), author.getId(), LocalDateTime.of(2026, 5, 1, 10, 0));

        List<Comment> comments = commentRepository.findAllByIssueIdOrderByCreatedAtAsc(issue.getId());

        assertThat(comments)
                .extracting(Comment::getId)
                .containsExactly(earlierId, laterId);
    }

    @Test
    void deleteCommentByIdsDeletesOnlyWhenOwnerMatches() {
        User owner = userRepository.save(user("comment-owner"));
        User other = userRepository.save(user("comment-other"));
        Issue issue = issueRepository.save(issue(owner.getId(), "owned comment issue", IssueStatus.OPEN, null));
        Long commentId = insertComment("owned", issue.getId(), owner.getId(), LocalDateTime.of(2026, 5, 1, 10, 0));

        assertThat(commentRepository.deleteCommentByIds(commentId, other.getId())).isZero();
        assertThat(commentRepository.existsById(commentId)).isTrue();

        assertThat(commentRepository.deleteCommentByIds(commentId, owner.getId())).isEqualTo(1);
        assertThat(commentRepository.existsById(commentId)).isFalse();
    }

    private Long insertComment(String content, Long issueId, Long userId, LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO comments (content, type, attachment_key, created_at, updated_at, user_id, issue_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
                content,
                CommentType.DISCUSSION.name(),
                null,
                createdAt,
                null,
                userId,
                issueId
        );
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM comments", Long.class);
    }
}
