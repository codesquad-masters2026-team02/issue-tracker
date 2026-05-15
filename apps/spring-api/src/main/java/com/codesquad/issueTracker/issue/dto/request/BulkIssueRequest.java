package com.codesquad.issueTracker.issue.dto.request;

import com.codesquad.issueTracker.issue.IssueStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BulkIssueRequest(
        @NotEmpty List<Long> issueIds,
        @NotNull IssueStatus status
) {

}
