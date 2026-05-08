package com.codesquad.issueTracker.issue;

import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueRepository extends CrudRepository<Issue, Long> {

    @NonNull
    @Override
    public List<Issue> findAll();
}
