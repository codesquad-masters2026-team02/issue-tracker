CREATE INDEX idx_issues_status_id
    ON issues (status, id);

CREATE INDEX idx_issues_status_author_id
    ON issues (status, author_id, id);

CREATE INDEX idx_issues_status_milestone_id
    ON issues (status, milestone_id, id);

CREATE INDEX idx_issue_users_user_issue
ON issue_users (user_id, issue_id);

CREATE INDEX idx_issue_labels_label_issue
    ON issue_labels (label_id, issue_id);
