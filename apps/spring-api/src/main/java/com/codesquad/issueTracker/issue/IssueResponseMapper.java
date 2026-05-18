package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.issue.dto.response.IssueSummaryResponse;
import com.codesquad.issueTracker.label.Label;
import com.codesquad.issueTracker.label.dto.LabelSummaryResponse;
import com.codesquad.issueTracker.milestone.Milestone;
import com.codesquad.issueTracker.milestone.dto.MilestoneReferenceResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class IssueResponseMapper {
    public IssueSummaryResponse toResponse(Issue issue, Map<Long, Label> labelMap,
                                           Map<Long, Milestone> milestoneMap
    ) {
        List<LabelSummaryResponse> labels = issue.getLabels().stream()
                .map(IssueLabel::labelId)
                .map(labelMap::get)
                .filter(Objects::nonNull)
                .map(LabelSummaryResponse::from)
                .toList();

        MilestoneReferenceResponse milestone = Optional.ofNullable(issue.getMilestoneId())
                .map(milestoneMap::get)
                .map(MilestoneReferenceResponse::from)
                .orElse(null);

        return IssueSummaryResponse.from(issue, labels, milestone);
    }
}
