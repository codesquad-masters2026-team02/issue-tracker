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
    MILESTONE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 마일스톤을 찾을 수 없습니다"),
    USERNAME_TAKEN(HttpStatus.CONFLICT, "요청하신 유저네임은 이미 사용중입니다"),
    USER_INFO_NOT_MATCHED(HttpStatus.UNAUTHORIZED, "로그인 정보가 올바르지 않습니다"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유저 정보가 일치하지 않습니다"),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
    private final HttpStatus status;
    private final String message;
}
