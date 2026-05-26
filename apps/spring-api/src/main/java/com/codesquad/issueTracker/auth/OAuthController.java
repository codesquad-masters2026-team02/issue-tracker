package com.codesquad.issueTracker.auth;

import com.codesquad.issueTracker.auth.dto.GithubLoginRequest;
import com.codesquad.issueTracker.common.response.ApiResponse;
import com.codesquad.issueTracker.user.UserService;
import com.codesquad.issueTracker.user.dto.AccessTokenResponse;
import com.codesquad.issueTracker.user.dto.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class OAuthController {
    private final UserService userService;

    @PostMapping("/github")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> githubLogin(@Valid @RequestBody GithubLoginRequest request) {
        TokenResponse response = userService.login(request.code());
        ResponseCookie cookie = ResponseCookie.from("refreshToken", response.refreshToken())
                .httpOnly(true)
//                .secure(true)
                .path("/api/users/refresh")
                .maxAge(14 * 24 * 60 * 60)
//                .sameSite("Strict")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(
                ApiResponse.ok(new AccessTokenResponse(response.accessToken())));
    }
}
