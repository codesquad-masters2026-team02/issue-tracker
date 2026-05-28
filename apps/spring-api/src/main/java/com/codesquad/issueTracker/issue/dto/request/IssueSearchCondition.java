package com.codesquad.issueTracker.issue.dto.request;

import com.codesquad.issueTracker.issue.IssueStatus;

import java.util.List;

public record IssueSearchCondition(
        IssueStatus status
        , List<Long> assigneeIds
        , List<Long> labelIds
        , Long milestoneId
        , Long authorId
        , Integer pageNumber
) {
    public IssueSearchCondition {
        if (status == null) {
            status = IssueStatus.OPEN;
        }
        if (pageNumber == null || pageNumber < 0) {
            pageNumber = 0;
        }
    }
}
