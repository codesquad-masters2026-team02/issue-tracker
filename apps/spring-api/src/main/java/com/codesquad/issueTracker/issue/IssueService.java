package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.issue.dto.IssueRequest;
import com.codesquad.issueTracker.issue.dto.IssueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issueRepository;

    public IssueResponse create(IssueRequest request) {
        Issue issue = request.toEntity();
        issueRepository.save(issue);
        return IssueResponse.from(issue);
    }

}
