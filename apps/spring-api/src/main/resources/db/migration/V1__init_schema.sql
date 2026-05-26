CREATE TABLE milestones(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    due_date DATE,
    description TEXT,
    status VARCHAR(50),
    is_deleted BOOLEAN
);
CREATE TABLE users(
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      username VARCHAR(64),
                      password VARCHAR(128),
                      refresh_token TEXT,
                      oauth_provider VARCHAR(50),
                      oauth_id VARCHAR(255),
                      profile_image_url VARCHAR(512)
);
CREATE TABLE labels (
                        id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name             VARCHAR(50) NOT NULL,
                        description      VARCHAR(100),
                        background_color CHAR(7) NOT NULL,
                        text_color       VARCHAR(7) NOT NULL
);
CREATE TABLE issues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id    BIGINT,
    title        VARCHAR(255),
    status       VARCHAR(50),
    created_at   DATETIME,
    milestone_id BIGINT,
    CONSTRAINT fk_milestone FOREIGN KEY (milestone_id) REFERENCES milestones(id)
);
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT,
    type VARCHAR(16) NOT NULL,
    attachment_key VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    user_id BIGINT,
    issue_id BIGINT,
    CONSTRAINT fk_issue_id FOREIGN KEY (issue_id) REFERENCES issues(id)
);

CREATE TABLE issue_labels
(
    issue_id BIGINT,
    label_id     BIGINT,
    PRIMARY KEY (issue_id, label_id),
    FOREIGN KEY (issue_id) REFERENCES issues (id) ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES labels (id) ON DELETE CASCADE
);

CREATE TABLE issue_users(
    issue_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (issue_id, user_id),
    FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_label_id ON issue_labels (label_id);

CREATE TABLE attachments (
    --     id              UUID PRIMARY KEY,
    id VARCHAR(36) PRIMARY KEY,
    s3_key          TEXT NOT NULL,
    filename        TEXT NOT NULL,
    content_type    TEXT NOT NULL,
    size_bytes      BIGINT NOT NULL,

    uploader_id     BIGINT NOT NULL,
    comment_id      BIGINT,

    --     status          TEXT NOT NULL DEFAULT 'PENDING',
    status VARCHAR(64) NOT NULL DEFAULT 'PENDING',
    -- 'PENDING': presign 발급됨, 본문 제출 안 됨
    -- 'COMMITTED': 이슈/코멘트에 실제로 사용됨

    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    committed_at    TIMESTAMP
);
