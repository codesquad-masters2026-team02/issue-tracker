package com.codesquad.issueTracker.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.security.PasswordHelper;
import com.codesquad.issueTracker.user.dto.LoginRequest;
import com.codesquad.issueTracker.user.dto.SignupRequest;
import com.codesquad.issueTracker.user.dto.TokenResponse;
import com.codesquad.issueTracker.user.dto.UserInfoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserServiceTest {

    private static final String VALID_PASSWORD = "Password1!";

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Test
    void signupCreatesUserWithHashedPassword() {
        userService.handleSignupRequest(new SignupRequest("new-user", VALID_PASSWORD));

        User saved = userRepository.findUserByUsername("new-user");
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPassword()).isNotEqualTo(VALID_PASSWORD);
        assertThat(PasswordHelper.verifyPassword(VALID_PASSWORD, saved.getPassword())).isTrue();
    }

    @Test
    void duplicateUsernameIsRejected() {
        userService.handleSignupRequest(new SignupRequest("dupe-user", VALID_PASSWORD));

        assertThatThrownBy(() -> userService.handleSignupRequest(new SignupRequest("dupe-user", VALID_PASSWORD)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USERNAME_TAKEN);
    }

    @Test
    void loginReturnsTokensAndStoresRefreshToken() {
        userService.handleSignupRequest(new SignupRequest("login-user", VALID_PASSWORD));

        TokenResponse token = userService.handleLoginRequest(new LoginRequest("login-user", VALID_PASSWORD));

        assertThat(token.accessToken()).isNotBlank();
        assertThat(token.refreshToken()).isNotBlank();
        assertThat(userRepository.findUserByUsername("login-user").getRefresh_token())
                .isEqualTo(token.refreshToken());
    }

    @Test
    void loginWithWrongPasswordIsRejected() {
        userService.handleSignupRequest(new SignupRequest("wrong-password-user", VALID_PASSWORD));

        assertThatThrownBy(() -> userService.handleLoginRequest(
                new LoginRequest("wrong-password-user", "Wrongpass1!")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_INFO_NOT_MATCHED);
    }

    @Test
    void refreshAccessTokenRequiresStoredRefreshTokenMatch() {
        userService.handleSignupRequest(new SignupRequest("refresh-user", VALID_PASSWORD));
        TokenResponse token = userService.handleLoginRequest(new LoginRequest("refresh-user", VALID_PASSWORD));

        TokenResponse refreshed = userService.refreshAccessToken(token.refreshToken());

        assertThat(refreshed.accessToken()).isNotBlank();
        assertThat(refreshed.refreshToken()).isNull();
    }

    @Test
    void logoutClearsRefreshToken() {
        userService.handleSignupRequest(new SignupRequest("logout-user", VALID_PASSWORD));
        userService.handleLoginRequest(new LoginRequest("logout-user", VALID_PASSWORD));
        User user = userRepository.findUserByUsername("logout-user");

        userService.handleLogoutRequest(user.getId());

        assertThat(userRepository.findById(user.getId())).get()
                .extracting(User::getRefresh_token)
                .isNull();
    }

    @Test
    void findUserInfoReturnsUserDto() {
        userService.handleSignupRequest(new SignupRequest("info-user", VALID_PASSWORD));
        User user = userRepository.findUserByUsername("info-user");

        UserInfoResponse response = userService.findUserInfo(user.getId());

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.username()).isEqualTo("info-user");
    }
}
