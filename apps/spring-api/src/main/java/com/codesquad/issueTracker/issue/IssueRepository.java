package com.codesquad.issueTracker.issue;

import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends ListCrudRepository<Issue, Long> {
    List<Issue> findByStatus(IssueStatus status);

    long countByStatus(IssueStatus status);
}
