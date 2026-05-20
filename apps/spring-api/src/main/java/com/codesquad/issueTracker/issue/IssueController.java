package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.common.response.ApiResponse;
import com.codesquad.issueTracker.issue.dto.request.BulkIssueRequest;
import com.codesquad.issueTracker.issue.dto.request.IssueRequest;
import com.codesquad.issueTracker.issue.dto.response.IssueDetailResponse;
import com.codesquad.issueTracker.issue.dto.response.IssueSearchResponse;
import com.codesquad.issueTracker.issue.dto.response.IssueSummaryResponse;
import com.codesquad.issueTracker.issue.dto.request.IssueSearchCondition;
import com.codesquad.issueTracker.issue.dto.request.UpdateIssueStatusRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {
    private final IssueService issueService;


    @GetMapping
    public ResponseEntity<ApiResponse<IssueSearchResponse>> mainPage(@ModelAttribute IssueSearchCondition condition) {
        IssueSearchResponse responses = issueService.getIssues(condition);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IssueDetailResponse>> issueDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.findIssueById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IssueDetailResponse>> createIssue(
            @RequestBody IssueRequest request,
            HttpServletRequest servletRequest
    ) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        IssueDetailResponse created = issueService.create(request, userId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.issueNumber())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(ApiResponse.ok(created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueStatusRequest request
    ) {
        issueService.updateStatus(id,request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }


    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<Void>> bulkUpdateStatus(@Valid @RequestBody BulkIssueRequest request) {
        issueService.bulkUpdateStatus(request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

}
