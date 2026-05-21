package com.codesquad.issueTracker.attachment;

import com.codesquad.issueTracker.attachment.dto.PresignRequest;
import com.codesquad.issueTracker.attachment.dto.PresignResponse;
import com.codesquad.issueTracker.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attachments")
public class AttachmentController {
    private final AttachmentService attachmentService;

    @PostMapping("/presign")
    public ResponseEntity<ApiResponse<PresignResponse>> presign(
            @RequestBody @Valid PresignRequest request,
            HttpServletRequest servletRequest
    ) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        PresignResponse presignedUpload = attachmentService.createPresignedUpload(request, userId);
        return ResponseEntity.ok(ApiResponse.ok(presignedUpload));
    }
}
