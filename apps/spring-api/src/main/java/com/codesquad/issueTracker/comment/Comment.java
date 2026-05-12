package com.codesquad.issueTracker.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="COMMENTS")
public class Comment {

    @Column("ID")
    @Id
    private Long id;
    @Column("CONTENT")
    private String content;
    @Column("TYPE")
    private String type;
    @Column("ATTACHMENT_KEY")
    private String attachmentKey;
    @Column("CREATED_AT")
    private LocalDateTime createdAt;
    @Column("UPDATED_AT")
    private LocalDateTime updatedAt;
    @Column("USER_ID")
    private Long userId;
    @Column("ISSUE_NUMBER")
    private Long issueNumber;
}
