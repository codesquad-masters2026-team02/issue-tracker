package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.comment.CommentService;
import com.codesquad.issueTracker.comment.CommentType;
import com.codesquad.issueTracker.comment.dto.CommentRequest;
import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.issue.dto.BulkIssueRequest;
import com.codesquad.issueTracker.issue.dto.IssueRequest;
import com.codesquad.issueTracker.issue.dto.IssueResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

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
    public void close(Long id) {
        Issue issue = findById(id);
        issue.close();
        issueRepository.save(issue);
    }

    @Transactional
    public void reopen(Long id) {
        Issue issue = findById(id);
        issue.reopen();
        issueRepository.save(issue);
    }

    @Transactional
    public void bulkClose(BulkIssueRequest request) {
        List<Issue> issues = issueRepository.findAllById(request.issueIds());

        for (Issue issue : issues) {
            issue.close();
        }
        issueRepository.saveAll(issues);
    }

    @Transactional
    public void bulkReopen(BulkIssueRequest request) {
        List<Issue> issues = issueRepository.findAllById(request.issueIds());

        for (Issue issue : issues) {
            issue.reopen();
        }
        issueRepository.saveAll(issues);
    }

    private Issue findById(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ISSUE_NOT_FOUND));
    }

}
