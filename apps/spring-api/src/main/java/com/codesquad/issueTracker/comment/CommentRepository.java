package com.codesquad.issueTracker.comment;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends ListCrudRepository<Comment,Long> {

    @Query("SELECT * FROM comments WHERE issue_id = :issueId ORDER BY created_at ASC")
    List<Comment> findAllByIssueIdOrderByCreatedAtAsc(@Param("issueId") Long issueId);

    @Modifying
    @Query("DELETE FROM comments WHERE id = :id AND user_id = :userId")
    int deleteCommentByIds(@Param("id") Long id, @Param("userId") Long userId);
}
