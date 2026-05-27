package com.codesquad.issueTracker.attachment;

import com.codesquad.issueTracker.attachment.dto.PresignRequest;
import com.codesquad.issueTracker.attachment.dto.PresignResponse;
import com.codesquad.issueTracker.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attachments")
public class AttachmentController {
    private final AttachmentService attachmentService;

    @GetMapping("/{id}/url")
    public ResponseEntity<ApiResponse<String>> getPresignedUrl(
            @PathVariable UUID id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String presignedUrl = attachmentService.getViewUrl(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(presignedUrl));
    }

    @PostMapping("/presign")
    public ResponseEntity<ApiResponse<PresignResponse>> presign(
            @RequestBody @Valid PresignRequest request,
            HttpServletRequest servletRequest
    ) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        PresignResponse presignedUpload = attachmentService.createAttachmentPresignedURL(request, userId);
        return ResponseEntity.ok(ApiResponse.ok(presignedUpload));
    }

    @PostMapping("/presign/profile")
    public ResponseEntity<ApiResponse<PresignResponse>> presignProfile(
            @RequestBody @Valid PresignRequest request,
            HttpServletRequest servletRequest
    ) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        PresignResponse presignedUpload = attachmentService.createProfilePresignURL(request, userId);
        return ResponseEntity.ok(ApiResponse.ok(presignedUpload));
    }
}
