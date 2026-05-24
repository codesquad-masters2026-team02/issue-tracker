package com.codesquad.issueTracker.milestone;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table(name = "milestones")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Milestone {
    @Id
    private Long id;
    private String name;
    private LocalDate dueDate;
    private String description;
    private MilestoneStatus status;
    private Boolean isDeleted;

    public void update(String name, LocalDate dueDate, String description){
        this.name = name;
        this.dueDate = dueDate;
        this.description = description;
    }

    public void changeStatus(MilestoneStatus status){
        this.status = status;
    }
}
