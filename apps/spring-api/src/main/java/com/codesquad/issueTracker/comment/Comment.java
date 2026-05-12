package com.codesquad.issueTracker.comment;

import com.codesquad.issueTracker.issue.Issue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="COMMENTS")
public class Comment {

    @Id
    private Long id;
    private String content;
    private String type;
    private String attachmentKey;
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private AggregateReference<Issue,Long> issueNumber;
}
