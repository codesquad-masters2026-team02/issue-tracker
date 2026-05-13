package com.codesquad.issueTracker.issue;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("ISSUES")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Issue {
    @Id
    private Long issueNumber;
    private Long authorId;
    private String title;
    private IssueStatus status;
    @CreatedDate
    private LocalDateTime createdAt;
    private Long milestoneId;

    @MappedCollection(idColumn = "ISSUE_NUMBER")
    private Set<IssueLabel> labels = new HashSet<>();
}
