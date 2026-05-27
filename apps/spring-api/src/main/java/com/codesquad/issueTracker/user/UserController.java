package com.codesquad.issueTracker.user;

import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.user.dto.AccessTokenResponse;
import com.codesquad.issueTracker.user.dto.LoginRequest;
import com.codesquad.issueTracker.user.dto.ProfileEditRequest;
import com.codesquad.issueTracker.user.dto.SignupRequest;
import com.codesquad.issueTracker.common.response.ApiResponse;
import com.codesquad.issueTracker.user.dto.TokenResponse;
import com.codesquad.issueTracker.user.dto.UserInfoResponse;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserInfoResponse>>> getUserList(){
        return ResponseEntity.ok(ApiResponse.ok(service.findAllUsers()));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> postSignupRequest(@RequestBody @Valid SignupRequest request){
        service.handleSignupRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.noContent());
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> postSignInRequest(@RequestBody @Valid LoginRequest request){
        TokenResponse response = service.handleLoginRequest(request);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", response.refreshToken())
                .httpOnly(true)
//                .secure(true)
                .path("/api/users/refresh")
                .maxAge(14*24*60*60)
//                .sameSite("Strict")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(ApiResponse.ok(new AccessTokenResponse(response.accessToken())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> refreshAccessToken(@CookieValue(name="refreshToken", defaultValue = "none") String refreshToken){
        if(refreshToken.equals("none")){
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        AccessTokenResponse response = new AccessTokenResponse(service.refreshAccessToken(refreshToken).accessToken());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(ApiResponse.ok(service.findUserInfo(userId)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> handleLogoutRequest(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        service.handleLogoutRequest(userId);

        ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/api/users/refresh")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .body(ApiResponse.noContent());
    }

    @PostMapping("/edit")
    public ResponseEntity<ApiResponse<Void>> editProfile(@Valid @RequestBody ProfileEditRequest request, HttpServletRequest servletRequest) {
        Long userId = (Long) servletRequest.getAttribute("userId");

        service.editProfile(request, userId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
