package com.codesquad.issueTracker.issue.dto;

import com.codesquad.issueTracker.issue.Issue;
import com.codesquad.issueTracker.issue.IssueLabel;
import com.codesquad.issueTracker.issue.IssueStatus;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record IssueRequest(
        String title,
        String content,
        List<Long> labelIds
) {
    public Issue toEntity() {
        return new Issue(null, null, title, IssueStatus.OPEN, null, null, toIssueLabels());
    }

    private Set<IssueLabel> toIssueLabels() {
        return labelIds().stream()
                .map(IssueLabel::new)
                .collect(Collectors.toSet());
    }
}
