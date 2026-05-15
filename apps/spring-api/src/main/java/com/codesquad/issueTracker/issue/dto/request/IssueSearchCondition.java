package com.codesquad.issueTracker.issue.dto.request;

import com.codesquad.issueTracker.issue.IssueStatus;

public record IssueSearchCondition(
        IssueStatus status
) {
    public IssueSearchCondition {
        if (status == null) {
            status = IssueStatus.OPEN;
        }
    }
}
