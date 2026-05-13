package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.comment.Comment;
import com.codesquad.issueTracker.comment.CommentService;
import com.codesquad.issueTracker.comment.CommentType;
import com.codesquad.issueTracker.comment.dto.CommentRequest;
import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.issue.dto.IssueRequest;
import com.codesquad.issueTracker.issue.dto.IssueResponse;
import java.util.Set;
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
        return issues.stream().map(this::mapIssueToDto).collect(Collectors.toList());
    }

    private IssueResponse mapIssueToDto(Issue issue){
        return IssueResponse.from(issue);
    }

    public IssueResponse findIssueById(Long id){
        Issue issue = issueRepository.findById(id).orElseThrow(()-> new BusinessException(ErrorCode.ISSUE_NOT_FOUND));
        return IssueResponse.from(issue);
    }
}
