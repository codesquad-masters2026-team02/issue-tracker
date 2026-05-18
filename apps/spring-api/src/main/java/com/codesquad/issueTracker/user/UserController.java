package com.codesquad.issueTracker.user;

import com.codesquad.issueTracker.user.dto.LoginRequest;
import com.codesquad.issueTracker.user.dto.SignupRequest;
import com.codesquad.issueTracker.common.response.ApiResponse;
import com.codesquad.issueTracker.user.dto.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> postSignupRequest(@RequestBody @Valid SignupRequest request){
        service.handleSignupRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.noContent());
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<String>> postSignInRequest(@RequestBody @Valid LoginRequest request){
        TokenResponse response = service.handleLoginRequest(request);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", response.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/users/refresh")
                .maxAge(14*24*60*60)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(ApiResponse.ok(response.accessToken()));
    }


}
