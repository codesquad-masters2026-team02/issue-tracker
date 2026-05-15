package com.codesquad.issueTracker.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류 발생"),
    ISSUE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 이슈를 찾을 수 없습니다"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 코멘트를 찾을 수 없습니다"),
    LABEL_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 레이블을 찾을 수 없습니다"),
    MILESTONE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 마일스톤을 찾을 수 없습니다");
    private final HttpStatus status;
    private final String message;
}
