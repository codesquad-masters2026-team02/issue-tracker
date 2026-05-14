package com.codesquad.issueTracker.issue;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends ListCrudRepository<Issue, Long> {
}
