package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.comment.CommentService;
import com.codesquad.issueTracker.comment.CommentType;
import com.codesquad.issueTracker.comment.dto.CommentRequest;
import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.issue.dto.request.BulkIssueRequest;
import com.codesquad.issueTracker.issue.dto.request.IssueRequest;
import com.codesquad.issueTracker.issue.dto.response.FilteredIssuesResponse;
import com.codesquad.issueTracker.issue.dto.response.IssueResponse;
import com.codesquad.issueTracker.issue.dto.request.IssueSearchCondition;
import com.codesquad.issueTracker.issue.dto.request.UpdateIssueStatusRequest;
import com.codesquad.issueTracker.milestone.MilestoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issueRepository;
    private final CommentService commentService;
    private final MilestoneService milestoneService;


    @Transactional
    public IssueResponse create(IssueRequest request) {
        Issue issue = request.toEntity();

        if(issue.getMilestoneId() != null && !milestoneService.findMilestoneExistenceById(issue.getMilestoneId())){
            throw new BusinessException(ErrorCode.MILESTONE_NOT_FOUND);
        }

        Issue saved = issueRepository.save(issue);

        CommentRequest issueBodyRequest = new CommentRequest(request.content());
        commentService.postComment(saved.getIssueNumber(),issueBodyRequest,CommentType.ISSUE_BODY);

        return IssueResponse.from(saved);
    }

    public FilteredIssuesResponse getIssues(IssueSearchCondition condition) {
        List<Issue> issues = issueRepository.findByStatus(condition.status());
        long openIssueCount = issueRepository.countByStatus(IssueStatus.OPEN);
        long closedIssueCount = issueRepository.countByStatus(IssueStatus.CLOSED);

        return FilteredIssuesResponse.from(openIssueCount, closedIssueCount, issues);
    }

    public IssueResponse findIssueById(Long id){
        Issue issue = findById(id);
        return IssueResponse.from(issue);
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

}
