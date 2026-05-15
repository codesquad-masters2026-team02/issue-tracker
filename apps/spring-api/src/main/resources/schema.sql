DROP TABLE IF EXISTS issues;
DROP TABLE IF EXISTS labels;
DROP TABLE IF EXISTS issue_labels;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS milestones;

CREATE TABLE milestones(
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   name VARCHAR(100),
   due_date DATE,
   description TEXT,
   open_issue_count INT,
   closed_issue_count INT,
   status VARCHAR(50),
   is_deleted BOOLEAN
);
CREATE TABLE issues (
    issue_number BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id    BIGINT,
    title        VARCHAR(255),
    status       VARCHAR(50),
    created_at   DATETIME,
    milestone_id BIGINT,
    CONSTRAINT fk_milestone FOREIGN KEY (milestone_id) REFERENCES milestones(id)
);
CREATE TABLE labels (
    label_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(50) NOT NULL,
    description      VARCHAR(100),
    background_color CHAR(7) NOT NULL,
    text_color       VARCHAR(7) NOT NULL
);
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT,
    type VARCHAR(16) NOT NULL,
    attachment_key VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    user_id BIGINT,
    issue_number BIGINT,
    CONSTRAINT fk_issue_number FOREIGN KEY (issue_number) REFERENCES ISSUES(issue_number)
);
CREATE TABLE issue_labels
(
    issue_number BIGINT,
    label_id     BIGINT,
    PRIMARY KEY (issue_number, label_id),
    FOREIGN KEY (issue_number) REFERENCES issues (issue_number) ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES labels (label_id) ON DELETE CASCADE
);
CREATE INDEX idx_label_id ON issue_labels (label_id);
