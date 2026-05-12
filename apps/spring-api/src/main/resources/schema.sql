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
CREATE TABLE issue_labels (
    issue_number BIGINT,
    label_id     BIGINT,
    PRIMARY KEY (issue_number, label_id)
);
CREATE INDEX idx_label_id ON issue_labels (label_id);
