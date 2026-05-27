package com.codesquad.issueTracker.user;

import com.codesquad.issueTracker.auth.OAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "users")
@Getter
@AllArgsConstructor
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    private String refresh_token;
    private OAuthProvider oauthProvider;
    private String oauthId;
    private String profileImageUrl;

    public void editProfile(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
