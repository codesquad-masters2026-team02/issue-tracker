package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.issue.dto.response.IssueSummaryResponse;
import com.codesquad.issueTracker.label.Label;
import com.codesquad.issueTracker.label.dto.LabelSummaryResponse;
import com.codesquad.issueTracker.milestone.Milestone;
import com.codesquad.issueTracker.milestone.dto.MilestoneReferenceResponse;
import com.codesquad.issueTracker.user.User;
import com.codesquad.issueTracker.user.dto.UserInfoResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class IssueResponseMapper {
    public IssueSummaryResponse toResponse(
            Issue issue, Map<Long, User> authorMap, Map<Long, Label> labelMap,
            Map<Long, Milestone> milestoneMap, Map<Long, User> assigneeMap
    ) {
        List<LabelSummaryResponse> labels = issue.getLabels().stream()
                .map(IssueLabel::labelId)
                .map(labelMap::get)
                .filter(Objects::nonNull)
                .map(LabelSummaryResponse::from)
                .toList();

        List<UserInfoResponse> assignees = issue.getUsers().stream()
                .map(IssueUser::userId)
                .map(assigneeMap::get)
                .filter(Objects::nonNull)
                .map(UserInfoResponse::from)
                .toList();


        MilestoneReferenceResponse milestone = Optional.ofNullable(issue.getMilestoneId())
                .map(milestoneMap::get)
                .map(MilestoneReferenceResponse::from)
                .orElse(null);

        String authorName = authorMap.get(issue.getAuthorId()).getUsername();

        return IssueSummaryResponse.from(issue, authorName, labels, milestone, assignees);
    }
}
