package com.codesquad.issueTracker.user.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
