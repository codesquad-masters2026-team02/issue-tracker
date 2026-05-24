package com.codesquad.issueTracker.attachment;

import com.codesquad.issueTracker.comment.Comment;
import com.codesquad.issueTracker.user.User;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table("attachments")
public class Attachment implements Persistable<UUID> {
    @Id
    private UUID id;

    private String s3Key;
    private String filename;
    private String contentType;
    private Long sizeBytes;
    private AggregateReference<User, Long> uploaderId;
    private AggregateReference<Comment, Long> commentId;

    private AttachmentStatus status;

    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime committedAt;

    @Transient
    private boolean isNew = false;

    public static Attachment createPending(
            UUID id, String s3Key, String fileName, String contentType, Long sizeBytes, Long uploaderId
    ) {
        Attachment a = new Attachment();
        a.id = id;
        a.s3Key = s3Key;
        a.filename = fileName;
        a.contentType = contentType;
        a.sizeBytes = sizeBytes;
        a.uploaderId = AggregateReference.to(uploaderId);
        a.status = AttachmentStatus.PENDING;
        a.createdAt = LocalDateTime.now();
        a.isNew = true;
        return a;
    }

    public void commit(Long commentId) {
        this.status = AttachmentStatus.COMMITTED;
        this.commentId = AggregateReference.to(commentId);
        this.committedAt = LocalDateTime.now();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}


