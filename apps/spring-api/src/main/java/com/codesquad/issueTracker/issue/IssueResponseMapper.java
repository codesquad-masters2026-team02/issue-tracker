package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.issue.dto.response.IssueResponse;
import com.codesquad.issueTracker.label.Label;
import com.codesquad.issueTracker.label.dto.LabelSummaryResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class IssueResponseMapper {
    public IssueResponse toResponse(Issue issue, Map<Long, Label> labelMap) {
        List<LabelSummaryResponse> labels = issue.getLabels().stream()
                .map(IssueLabel::labelId)
                .map(labelMap::get)
                .filter(Objects::nonNull)
                .map(LabelSummaryResponse::from)
                .toList();

        return IssueResponse.from(issue, labels);
    }
}
