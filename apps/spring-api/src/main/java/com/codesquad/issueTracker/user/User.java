package com.codesquad.issueTracker.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "USERS")
@Getter
@AllArgsConstructor
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    private String refresh_token;
    private String oauthProvider;
    private String oauthId;
    private String profileImageUrl;
}
