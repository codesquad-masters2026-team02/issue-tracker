package com.codesquad.issueTracker.issue;

import org.springframework.data.relational.core.mapping.Column;

public record IssueLabel(@Column("LABEL_ID") Long labelId) {
}
