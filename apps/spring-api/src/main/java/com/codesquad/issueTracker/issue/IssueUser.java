package com.codesquad.issueTracker.issue;

import org.springframework.data.relational.core.mapping.Table;

@Table("issue_users")
public record IssueUser(Long userId) {
}
