package com.codesquad.issueTracker.comment;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends ListCrudRepository<Comment,Long> {
}
