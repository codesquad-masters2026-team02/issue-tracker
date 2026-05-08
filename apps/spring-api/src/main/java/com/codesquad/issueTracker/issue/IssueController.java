package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.common.response.ApiResponse;
import com.codesquad.issueTracker.issue.dto.IssueRequest;
import com.codesquad.issueTracker.issue.dto.IssueResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {
    private final IssueService issueService;

    @GetMapping
    public ResponseEntity<ApiResponse<Void>> test() {
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IssueResponse>> createIssue(@RequestBody IssueRequest request) {
        IssueResponse created = issueService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.issueNumber())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(ApiResponse.ok(created));
    }

}
