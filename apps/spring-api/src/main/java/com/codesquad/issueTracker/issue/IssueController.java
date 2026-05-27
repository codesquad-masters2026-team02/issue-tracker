package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.common.response.ApiResponse;
import com.codesquad.issueTracker.issue.dto.request.*;
import com.codesquad.issueTracker.issue.dto.response.IssueDetailResponse;
import com.codesquad.issueTracker.issue.dto.response.IssueSearchResponse;
import com.codesquad.issueTracker.issue.dto.response.IssueSummaryResponse;
import com.codesquad.issueTracker.label.dto.LabelUpdateRequest;
import com.codesquad.issueTracker.milestone.dto.MilestoneSidebarUpdateRequest;
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

    @PatchMapping("/{id}/title")
    public ResponseEntity<ApiResponse<Void>> updateTitle(
            @PathVariable Long id,
            @Valid @RequestBody IssueTitleUpdateRequest request
    ) {
        issueService.updateTitle(id, request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }


    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<Void>> bulkUpdateStatus(@Valid @RequestBody BulkIssueRequest request) {
        issueService.bulkUpdateStatus(request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PostMapping("/{id}/assignees")
    public ResponseEntity<ApiResponse<Void>> updateAssignedUser(@PathVariable Long id, @RequestBody @Valid AssigneeUpdateRequest request){
        issueService.updateAssignees(id, true, request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PostMapping("/{id}/assignees/remove")
    public ResponseEntity<ApiResponse<Void>> updateUnassignedUser(@PathVariable Long id, @RequestBody @Valid AssigneeUpdateRequest request) {
        issueService.updateAssignees(id, false, request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PostMapping("/{id}/labels")
    public ResponseEntity<ApiResponse<Void>> updateLabelByIssueId(@PathVariable Long id, @RequestBody @Valid LabelUpdateRequest request){
        issueService.updateLabel(id, true, request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PostMapping("/{id}/labels/remove")
    public ResponseEntity<ApiResponse<Void>> updateRemovedLabelsByIssueId(@PathVariable Long id,  @RequestBody @Valid LabelUpdateRequest request){
        issueService.updateLabel(id, false, request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PostMapping("/{id}/milestones")
    public ResponseEntity<ApiResponse<Void>> updateMilestoneByIssueId(@PathVariable Long id, @RequestBody @Valid MilestoneSidebarUpdateRequest request){
        issueService.updateMilestone(id, true, request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PostMapping("/{id}/milestones/remove")
    public ResponseEntity<ApiResponse<Void>> updateRemovedMilestoneByIssueId(@PathVariable Long id){
        issueService.updateMilestone(id, false, null);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
