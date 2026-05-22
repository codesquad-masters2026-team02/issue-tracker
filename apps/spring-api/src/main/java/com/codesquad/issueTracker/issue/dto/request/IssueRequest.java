package com.codesquad.issueTracker.issue.dto.request;

import com.codesquad.issueTracker.issue.Issue;
import com.codesquad.issueTracker.issue.IssueLabel;
import com.codesquad.issueTracker.issue.IssueStatus;
import com.codesquad.issueTracker.issue.IssueUser;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record IssueRequest(
        String title,
        String content,
        List<Long> labelIds,
        Long milestoneId,
        List<Long> userIds,
        List<UUID> attachmentIds
) {
    public Issue toEntity(Long authorId) {
        return new Issue(null, authorId, title, IssueStatus.OPEN, null, milestoneId, toIssueLabels(), toIssueUsers());
    }

    private Set<IssueLabel> toIssueLabels() {
        return labelIds().stream()
                .map(IssueLabel::new)
                .collect(Collectors.toSet());
    }

    private Set<IssueUser> toIssueUsers(){
        return userIds.stream()
                .map(IssueUser::new).collect(Collectors.toSet());
    }
}
