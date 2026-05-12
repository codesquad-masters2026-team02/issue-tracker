package com.codesquad.issueTracker.comment;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends ListCrudRepository<Comment,Long> {

    List<Comment> findAllByIssueNumber(Long issueNumber);
}
