package com.codesquad.issueTracker.comment;

import com.codesquad.issueTracker.comment.dto.CommentListResponse;
import com.codesquad.issueTracker.comment.dto.CommentRequest;
import com.codesquad.issueTracker.comment.dto.CommentResponse;
import com.codesquad.issueTracker.common.response.ApiResponse;
import com.codesquad.issueTracker.common.response.ErrorDto;
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
    public ResponseEntity<ApiResponse<CommentResponse>> postCommentForIssue(@PathVariable Long id,@RequestBody CommentRequest request){
        CommentResponse response = service.postComment(id, request, CommentType.DISCUSSION);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/issues/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(response));
    }

    @GetMapping("/api/issues/{id}/comments")
    public ResponseEntity<ApiResponse<CommentListResponse>> getCommentsForIssue(@PathVariable Long id){
        CommentListResponse commentList = service.getCommentListForIssue(id);
        return ResponseEntity.ok(ApiResponse.ok(commentList));
    }

    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCommentByCommentId(@PathVariable Long id){
        service.deleteCommentByCommentId(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
