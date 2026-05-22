package com.codesquad.issueTracker.issue.dto.response;

import com.codesquad.issueTracker.user.User;

public record AssigneeSummaryResponse (
    Long id,
    String username,
    String profileImageUrl
){
    public static AssigneeSummaryResponse from(User user){
        return new AssigneeSummaryResponse(user.getId(),user.getUsername(),user.getProfileImageUrl());
    }
}
