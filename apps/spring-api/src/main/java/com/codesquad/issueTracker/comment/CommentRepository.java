package com.codesquad.issueTracker.comment;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends ListCrudRepository<Comment,Long> {

    List<Comment> findAllByIssueNumberOrderByCreatedAtAsc(Long issueNumber);

    @Modifying
    @Query("DELETE FROM COMMENTS WHERE id = :id")
    int deleteCommentById(@Param("id") Long id);
}
