DROP TABLE IF EXISTS issues;

CREATE TABLE issues (
    issue_number BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT,
    title VARCHAR(255),
    status VARCHAR(50),
    created_at TIMESTAMP,
    milestone_id BIGINT
);