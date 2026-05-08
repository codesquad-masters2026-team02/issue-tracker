package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.milestone.Milestone;
import com.codesquad.issueTracker.user.User;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "issues")
@Getter
@AllArgsConstructor
public class Issue {
    @Id
    @Column("issue_number")
    private Long issueNumber;
    @Column("author_id")
    private AggregateReference<User, Long> authorId;
    private String title;
    private IssueStatus status;
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createAt;
    @Column("milestone_id")
    private AggregateReference<Milestone, Long> milestoneId;
}
