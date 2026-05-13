DROP TABLE IF EXISTS issues;
DROP TABLE IF EXISTS labels;
DROP TABLE IF EXISTS issue_labels;

CREATE TABLE issues (
    issue_number BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id    BIGINT,
    title        VARCHAR(255),
    status       VARCHAR(50),
    created_at   DATETIME,
    milestone_id BIGINT
);
CREATE TABLE labels (
    label_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(50) NOT NULL,
    description      VARCHAR(100),
    background_color CHAR(7) NOT NULL,
    text_color       VARCHAR(7) NOT NULL
);

DROP TABLE IF EXISTS COMMENTS;

CREATE TABLE COMMENTS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    CONTENT TEXT,
    TYPE VARCHAR(16) NOT NULL,
    ATTACHMENT_KEY VARCHAR(255),
    CREATED_AT DATETIME NOT NULL,
    UPDATED_AT DATETIME,
    USER_ID BIGINT,
    ISSUE_NUMBER BIGINT,
    CONSTRAINT FK_ISSUE_NUMBER FOREIGN KEY (ISSUE_NUMBER) REFERENCES ISSUES(ISSUE_NUMBER)
)
CREATE TABLE issue_labels
(
    issue_number BIGINT,
    label_id     BIGINT,
    PRIMARY KEY (issue_number, label_id),
    FOREIGN KEY (issue_number) REFERENCES issues (issue_number) ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES labels (label_id) ON DELETE CASCADE
);
CREATE INDEX idx_label_id ON issue_labels (label_id);
