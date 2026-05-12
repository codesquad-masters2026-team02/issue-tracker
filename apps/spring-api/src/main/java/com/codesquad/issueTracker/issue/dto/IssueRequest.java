package com.codesquad.issueTracker.issue.dto;

import com.codesquad.issueTracker.issue.Issue;
import com.codesquad.issueTracker.issue.IssueStatus;

public record IssueRequest(
        String title,
        String content
) {
    public Issue toEntity() {
        return new Issue(null, null, title, IssueStatus.OPEN, null, null, null);
    }
}
