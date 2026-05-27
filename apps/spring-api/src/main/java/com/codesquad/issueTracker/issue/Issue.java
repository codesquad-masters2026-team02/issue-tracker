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

@Table("issues")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Issue {
    @Id
    private Long id;
    private Long authorId;
    private String title;
    private IssueStatus status;
    @CreatedDate
    private LocalDateTime createdAt;
    private Long milestoneId;

    @MappedCollection(idColumn = "issue_id")
    private Set<IssueLabel> labels = new HashSet<>();

    @MappedCollection(idColumn = "issue_id")
    private Set<IssueUser> users = new HashSet<>();


    public void assignUser(Long userId){
        this.users.add(new IssueUser(userId));
    }

    public void unassignUser(Long userId){
        this.users.removeIf(issueUser -> issueUser.userId().equals(userId));
    }

    public void assignLabel(Long labelId){
        this.labels.add(new IssueLabel(labelId));
    }

    public void unassignLabel(Long labelId){
        this.labels.removeIf(label -> label.labelId().equals(labelId));
    }

    public void updateMilestone(Long milestoneId){
        this.milestoneId = milestoneId;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void changeStatus(IssueStatus status) {
        this.status = status;
    }
}
