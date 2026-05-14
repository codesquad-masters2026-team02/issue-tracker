package com.codesquad.issueTracker.milestone;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "MILESTONES")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Milestone {
    @Id
    private Long id;
    private String name;
    private LocalDateTime dueDate;
    private String description;
    private Integer openIssueCount;
    private Integer closedIssueCount;
    private String status;
    private Boolean isDeleted;
}
