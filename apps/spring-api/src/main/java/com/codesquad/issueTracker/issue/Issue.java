package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.milestone.Milestone;
import com.codesquad.issueTracker.user.User;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "ISSUES")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Issue {
    @Id
    @Column("ISSUE_NUMBER")
    private Long issueNumber;
//    @Column("author_id")
//    private AggregateReference<User, Long> authorId;
    @Column("AUTHOR_ID")
    private Long authorId;
    @Column("TITLE")
    private String title;
    @Column("STATUS")
    private IssueStatus status;
    @CreatedDate
    @Column("CREATED_AT")
    private LocalDateTime createdAt;
//    @Column("milestone_id")
//    private AggregateReference<Milestone, Long> milestoneId;
    @Column("MILESTONE_ID")
    private Long milestoneId;

    @MappedCollection(idColumn = "ISSUE_NUMBER")
    private Set<IssueLabel> labels = new HashSet<>();
}
