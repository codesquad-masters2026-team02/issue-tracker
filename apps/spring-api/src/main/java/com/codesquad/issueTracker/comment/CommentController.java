package com.codesquad.issueTracker.comment;

import com.codesquad.issueTracker.comment.dto.CommentListResponse;
import com.codesquad.issueTracker.comment.dto.CommentRequest;
import com.codesquad.issueTracker.comment.dto.CommentResponse;
import com.codesquad.issueTracker.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService service;

    @PostMapping("/api/issues/{id}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> postCommentForIssue(@RequestBody CommentRequest request){
        CommentResponse response = service.postComment();
        URI uri = ServletUriComponentsBuilder.
    }

    @GetMapping("/api/issues/{id}/comments")
    public ResponseEntity<ApiResponse<CommentListResponse>> getCommentsForIssue(@PathVariable Long id, @){

    }
}
