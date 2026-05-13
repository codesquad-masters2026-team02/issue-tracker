package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.common.response.ApiResponse;
import com.codesquad.issueTracker.issue.dto.IssueRequest;
import com.codesquad.issueTracker.issue.dto.IssueResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {
    private final IssueService issueService;


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

    @GetMapping
    public ResponseEntity<ApiResponse<List<IssueResponse>>> mainPage(){
        List<IssueResponse> responses = issueService.getMainPageIssues();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IssueResponse>> issueDetail(@PathVariable Long id){
        return ResponseEntity.ok(ApiResponse.ok(issueService.findIssueById(id)));
    }
}
