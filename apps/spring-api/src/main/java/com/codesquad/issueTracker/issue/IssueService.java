package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.comment.CommentService;
import com.codesquad.issueTracker.comment.CommentType;
import com.codesquad.issueTracker.comment.dto.CommentRequest;
import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.issue.dto.BulkIssueRequest;
import com.codesquad.issueTracker.issue.dto.IssueRequest;
import com.codesquad.issueTracker.issue.dto.IssueResponse;
import com.codesquad.issueTracker.issue.dto.UpdateIssueStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issueRepository;
    private final CommentService commentService;

    @Transactional
    public IssueResponse create(IssueRequest request) {
        Issue issue = request.toEntity();
        Issue saved = issueRepository.save(issue);

        CommentRequest issueBodyRequest = new CommentRequest(request.content());
        commentService.postComment(saved.getIssueNumber(),issueBodyRequest,CommentType.ISSUE_BODY);

        return IssueResponse.from(saved);
    }


    public List<IssueResponse> getMainPageIssues(){
        List<Issue> issues = issueRepository.findAll();
        return issues.stream().map(IssueResponse::from).collect(Collectors.toList());
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
