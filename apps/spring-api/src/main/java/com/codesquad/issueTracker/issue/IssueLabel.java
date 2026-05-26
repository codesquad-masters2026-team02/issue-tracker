package com.codesquad.issueTracker.issue;

import org.springframework.data.relational.core.mapping.Table;

@Table("issue_labels")
public record IssueLabel(Long labelId) {
}
