package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.comment.CommentService;
import com.codesquad.issueTracker.comment.CommentType;
import com.codesquad.issueTracker.comment.dto.CommentRequest;
import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.issue.dto.request.BulkIssueRequest;
import com.codesquad.issueTracker.issue.dto.request.IssueRequest;
import com.codesquad.issueTracker.issue.dto.response.IssueDetailResponse;
import com.codesquad.issueTracker.issue.dto.response.IssueSearchResponse;
import com.codesquad.issueTracker.issue.dto.response.IssueSummaryResponse;
import com.codesquad.issueTracker.issue.dto.request.IssueSearchCondition;
import com.codesquad.issueTracker.issue.dto.request.UpdateIssueStatusRequest;
import com.codesquad.issueTracker.label.Label;
import com.codesquad.issueTracker.label.LabelRepository;
import com.codesquad.issueTracker.label.dto.LabelSummaryResponse;
import com.codesquad.issueTracker.milestone.Milestone;
import com.codesquad.issueTracker.milestone.MilestoneRepository;
import com.codesquad.issueTracker.milestone.MilestoneService;
import com.codesquad.issueTracker.milestone.dto.MilestoneSummaryResponse;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issueRepository;
    private final LabelRepository labelRepository;
    private final MilestoneRepository milestoneRepository;
    private final CommentService commentService;
    private final IssueResponseMapper issueResponseMapper;


    @Transactional
    public IssueDetailResponse create(IssueRequest request) {
        Issue issue = request.toEntity();
        Issue saved = issueRepository.save(issue);

        CommentRequest issueBodyRequest = new CommentRequest(request.content());
        commentService.postComment(saved.getId(),issueBodyRequest,CommentType.ISSUE_BODY);

        List<LabelSummaryResponse> labels = findLabelsByIssueLabels(saved);

        MilestoneSummaryResponse milestone = null;
        if (saved.getMilestoneId() != null) {
            Milestone found = milestoneRepository.findActiveMilestoneById(saved.getMilestoneId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MILESTONE_NOT_FOUND));
            milestone = MilestoneSummaryResponse.from(found);
        }
        return IssueDetailResponse.from(saved, labels, milestone);
    }

    public IssueSearchResponse getIssues(IssueSearchCondition condition) {
        long openIssueCount = issueRepository.countByStatus(IssueStatus.OPEN);
        long closedIssueCount = issueRepository.countByStatus(IssueStatus.CLOSED);
        List<Issue> issues = issueRepository.findByStatus(condition.status());

        Map<Long, Label> labelMap = findLabelMapByIssues(issues);
        Map<Long, Milestone> milestoneMap = findMilestoneMapByIssues(issues);

        List<IssueSummaryResponse> issueSummaryResponses = issues.stream()
                .map(issue -> issueResponseMapper.toResponse(issue, labelMap, milestoneMap))
                .toList();

        return IssueSearchResponse.from(openIssueCount, closedIssueCount, issueSummaryResponses);
    }

    public IssueDetailResponse findIssueById(Long id) {
        Issue issue = findById(id);
        List<LabelSummaryResponse> labels = findLabelsByIssueLabels(issue);

        MilestoneSummaryResponse milestone = null;

        if (issue.getMilestoneId() != null) {
            Milestone found = milestoneRepository.findActiveMilestoneById(issue.getMilestoneId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MILESTONE_NOT_FOUND));
            milestone = MilestoneSummaryResponse.from(found);
        }

        return IssueDetailResponse.from(issue, labels, milestone);
    }

    @Transactional
    public void updateStatus(Long id, UpdateIssueStatusRequest request) {
        Issue issue = findById(id);
        issue.changeStatus(request.status());
        issueRepository.save(issue);
    }

    @Transactional
    public void bulkUpdateStatus(BulkIssueRequest request) {
        List<Issue> issues = issueRepository.findAllById(request.issueIds());

        for (Issue issue : issues) {
            issue.changeStatus(request.status());
        }
        issueRepository.saveAll(issues);
    }

    private Issue findById(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ISSUE_NOT_FOUND));
    }

    private List<LabelSummaryResponse> findLabelsByIssueLabels(Issue issue) {
        List<Long> labelIds = issue.getLabels().stream()
                .map(IssueLabel::labelId)
                .toList();
        return labelRepository.findAllById(labelIds).stream()
                .map(LabelSummaryResponse::from)
                .toList();
    }

    private Map<Long, Label> findLabelMapByIssues(List<Issue> issues) {
        Set<Long> labelIds = issues.stream()
                .flatMap(issue -> issue.getLabels().stream())
                .map(IssueLabel::labelId)
                .collect(Collectors.toSet());

        if (labelIds.isEmpty()) {
            return Map.of();
        }

        return labelRepository.findAllById(labelIds).stream()
                .collect(Collectors.toMap(Label::getId, Function.identity()));
    }

    private Map<Long, Milestone> findMilestoneMapByIssues(List<Issue> issues) {
        Set<Long> milestoneIds = issues.stream()
                .map(Issue::getMilestoneId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (milestoneIds.isEmpty()) {
            return Map.of();
        }

        return milestoneRepository.findActiveAllByIds(milestoneIds).stream()
                .collect(Collectors.toMap(Milestone::getId, Function.identity()));
    }
}
