package com.codesquad.issueTracker.user.dto;

import com.codesquad.issueTracker.user.User;

public record UserInfoResponse(
        Long id,
        String username,
        String profileImageUrl
) {
    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(user.getId(), user.getUsername(), user.getProfileImageUrl());
    }
}
