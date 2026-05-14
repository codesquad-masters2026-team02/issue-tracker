package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.common.response.ApiResponse;
import com.codesquad.issueTracker.issue.dto.BulkIssueRequest;
import com.codesquad.issueTracker.issue.dto.IssueRequest;
import com.codesquad.issueTracker.issue.dto.IssueResponse;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {
    private final IssueService issueService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<IssueResponse>>> mainPage() {
        List<IssueResponse> responses = issueService.getMainPageIssues();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IssueResponse>> issueDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.findIssueById(id)));
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

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<Void>> closeIssue(@PathVariable Long id) {
        issueService.close(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<Void>> reopenIssue(@PathVariable Long id) {
        issueService.reopen(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PostMapping("/bulk-close")
    public ResponseEntity<ApiResponse<Void>> closeIssues(@Valid @RequestBody BulkIssueRequest request) {
        issueService.bulkClose(request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PostMapping("/bulk-reopen")
    public ResponseEntity<ApiResponse<Void>> reopenIssues(@Valid @RequestBody BulkIssueRequest request) {
        issueService.bulkReopen(request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
