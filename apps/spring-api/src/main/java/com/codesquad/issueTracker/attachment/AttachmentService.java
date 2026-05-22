package com.codesquad.issueTracker.attachment;

import com.codesquad.issueTracker.attachment.dto.AttachmentSummaryResponse;
import com.codesquad.issueTracker.attachment.dto.PresignRequest;
import com.codesquad.issueTracker.attachment.dto.PresignResponse;
import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final long MAX_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png", "image/jpeg", "image/gif", "image/webp", "application/pdf"
    );

    @Value("${aws.s3.bucket}")
    private String bucket;
    private final S3Presigner presigner;
    private final S3Client s3Client;

    private final AttachmentRepository attachmentRepository;


    public PresignResponse createPresignedUpload(PresignRequest request, Long userId) {
        if (request.size() > MAX_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        if (!ALLOWED_TYPES.contains(request.contentType())) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }

        UUID attachmentId = UUID.randomUUID();
        String extension = extractExtension(request.filename());
        String s3Key = String.format("attachments/%d%s%s", userId, attachmentId, extension);

        PutObjectRequest pubRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType(request.contentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(pubRequest)
                .build();

        String uploadUrl = presigner.presignPutObject(presignRequest).url().toString();

        Attachment attachment = Attachment.createPending(
                attachmentId, s3Key, request.filename(), request.contentType(), request.size(), userId
        );

        Attachment save = attachmentRepository.save(attachment);
        return new PresignResponse(uploadUrl, attachmentId.toString(), "/api/attachments/" + attachmentId);
    }

    public String getViewUrl(UUID attachmentId, Long userId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND));

        if (attachment.getStatus() == AttachmentStatus.PENDING || attachment.getCommentId() == null) {
            throw new BusinessException(ErrorCode.ATTACHMENT_ACCESS_DENIED);
        }

        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucket)
                .key(attachment.getS3Key())
                .responseContentType(attachment.getContentType())
                .responseContentDisposition(
                        "inline; filename=\"" +
                                URLEncoder.encode(attachment.getFilename(), StandardCharsets.UTF_8) +
                                "\"")
                .build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(getReq)
                .build();

        return presigner.presignGetObject(presignReq).url().toString();
    }

    public void commitToComment(List<UUID> attachmentIds, Long uploaderId, Long commentId) {
        if (attachmentIds == null || attachmentIds.isEmpty()) return;
        List<Attachment> attachments = attachmentRepository.findAllById(attachmentIds).stream()
                .filter(a -> a.getUploaderId().getId().equals(uploaderId))
                .filter(a -> a.getStatus() == AttachmentStatus.PENDING)
                .toList();
        for (Attachment attachment : attachments) {
            attachment.commit(commentId);
        }
        attachmentRepository.saveAll(attachments);
    }

    public Map<Long, List<AttachmentSummaryResponse>> getSummariesByCommentIds(Collection<Long> commentIds) {
        if (commentIds.isEmpty()) return Map.of();
        return attachmentRepository.findAllByCommentIdIn(commentIds).stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCommentId().getId(),
                        Collectors.mapping(AttachmentSummaryResponse::from, Collectors.toList())
                ));
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot);
    }
}
