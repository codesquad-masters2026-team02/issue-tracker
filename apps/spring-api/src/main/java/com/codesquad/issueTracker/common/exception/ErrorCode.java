package com.codesquad.issueTracker.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류 발생");

    private final HttpStatus status;
    private final String message;
}
