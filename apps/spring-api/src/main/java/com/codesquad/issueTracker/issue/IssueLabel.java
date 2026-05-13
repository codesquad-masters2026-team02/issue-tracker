package com.codesquad.issueTracker.issue;

import org.springframework.data.relational.core.mapping.Table;

@Table("ISSUE_LABELS")
public record IssueLabel(Long labelId) {
}
