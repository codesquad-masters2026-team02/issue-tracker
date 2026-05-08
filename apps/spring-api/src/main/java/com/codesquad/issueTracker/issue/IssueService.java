package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.issue.dto.IssueRequest;
import com.codesquad.issueTracker.issue.dto.IssueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issueRepository;

    public IssueResponse create(IssueRequest request) {
        Issue issue = request.toEntity();
        issueRepository.save(issue);
        return IssueResponse.from(issue);
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
