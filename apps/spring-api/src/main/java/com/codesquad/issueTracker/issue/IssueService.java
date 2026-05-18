package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.comment.CommentService;
import com.codesquad.issueTracker.comment.CommentType;
import com.codesquad.issueTracker.comment.dto.CommentRequest;
import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.issue.dto.request.BulkIssueRequest;
import com.codesquad.issueTracker.issue.dto.request.IssueRequest;
import com.codesquad.issueTracker.issue.dto.response.IssueSearchResponse;
import com.codesquad.issueTracker.issue.dto.response.IssueResponse;
import com.codesquad.issueTracker.issue.dto.request.IssueSearchCondition;
import com.codesquad.issueTracker.issue.dto.request.UpdateIssueStatusRequest;
import com.codesquad.issueTracker.label.Label;
import com.codesquad.issueTracker.label.LabelRepository;
import com.codesquad.issueTracker.label.dto.LabelSummaryResponse;
import com.codesquad.issueTracker.milestone.MilestoneService;
import java.util.Map;
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
    private final CommentService commentService;
    private final MilestoneService milestoneService;
    private final IssueResponseMapper issueResponseMapper;


    @Transactional
    public IssueResponse create(IssueRequest request) {
        Issue issue = request.toEntity();

        if(issue.getMilestoneId() != null && !milestoneService.findMilestoneExistenceById(issue.getMilestoneId())){
            throw new BusinessException(ErrorCode.MILESTONE_NOT_FOUND);
        }

        Issue saved = issueRepository.save(issue);

        CommentRequest issueBodyRequest = new CommentRequest(request.content());
        commentService.postComment(saved.getId(),issueBodyRequest,CommentType.ISSUE_BODY);

        List<LabelSummaryResponse> labels = findLabelsByIssueLabels(issue);
        return IssueResponse.from(saved, labels);
    }

    public IssueSearchResponse getIssues(IssueSearchCondition condition) {
        long openIssueCount = issueRepository.countByStatus(IssueStatus.OPEN);
        long closedIssueCount = issueRepository.countByStatus(IssueStatus.CLOSED);
        List<Issue> issues = issueRepository.findByStatus(condition.status());

        Set<Long> labelIds = issues.stream()
                .flatMap(issue -> issue.getLabels().stream())
                .map(IssueLabel::labelId)
                .collect(Collectors.toSet());

        Map<Long, Label> labelMap = labelRepository.findAllById(labelIds).stream()
                .collect(Collectors.toMap(Label::getId, Function.identity()));

        List<IssueResponse> issueResponses = issues.stream()
                .map(issue -> issueResponseMapper.toResponse(issue, labelMap))
                .toList();

        return IssueSearchResponse.from(openIssueCount, closedIssueCount, issueResponses);
    }

    public IssueResponse findIssueById(Long id) {
        Issue issue = findById(id);
        List<LabelSummaryResponse> labels = findLabelsByIssueLabels(issue);
        return IssueResponse.from(issue, labels);
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
}
