package com.codesquad.issueTracker.issue.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BulkIssueRequest(@NotEmpty List<Long> issueIds) {
}
