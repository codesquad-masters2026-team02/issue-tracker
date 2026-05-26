package com.codesquad.issueTracker;

import com.codesquad.issueTracker.issue.Issue;
import com.codesquad.issueTracker.issue.IssueLabel;
import com.codesquad.issueTracker.issue.IssueStatus;
import com.codesquad.issueTracker.issue.IssueUser;
import com.codesquad.issueTracker.label.Label;
import com.codesquad.issueTracker.label.TextColor;
import com.codesquad.issueTracker.milestone.Milestone;
import com.codesquad.issueTracker.milestone.MilestoneStatus;
import com.codesquad.issueTracker.user.User;
import java.time.LocalDate;
import java.util.Set;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static User user(String username) {
        return new User(null, username, "$2a$12$test-password-hash", null, null, null, null);
    }

    public static Label label(String name) {
        return new Label(null, name, name + " description", "#123456", TextColor.LIGHT);
    }

    public static Milestone milestone(String name) {
        return new Milestone(null, name, LocalDate.of(2026, 5, 31), name + " description", MilestoneStatus.OPEN, false);
    }

    public static Issue issue(Long authorId, String title, IssueStatus status, Long milestoneId) {
        return new Issue(null, authorId, title, status, null, milestoneId, Set.of(), Set.of());
    }

    public static Issue issue(
            Long authorId,
            String title,
            IssueStatus status,
            Long milestoneId,
            Set<IssueLabel> labels,
            Set<IssueUser> users
    ) {
        return new Issue(null, authorId, title, status, null, milestoneId, labels, users);
    }
}
