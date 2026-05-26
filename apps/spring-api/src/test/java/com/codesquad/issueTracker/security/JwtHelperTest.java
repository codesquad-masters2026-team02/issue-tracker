package com.codesquad.issueTracker.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class JwtHelperTest {

    private final JwtHelper jwtHelper = new JwtHelper(
            "ThisIsALocalSecretKeyForTestingAndThisWillBeReplacedWithSecretKeyInGithubAction",
            3_600_000,
            1_209_600_000
    );

    @Test
    void createsAccessTokenContainingUserId() {
        String token = jwtHelper.createUserAccessToken(42L);

        assertThat(jwtHelper.extractUserIdFromToken(token)).isEqualTo(42L);
    }

    @Test
    void createsRefreshTokenContainingUserId() {
        String token = jwtHelper.createUserRefreshToken(99L);

        assertThat(jwtHelper.extractUserIdFromToken(token)).isEqualTo(99L);
    }

    @Test
    void invalidTokenThrowsTokenInvalid() {
        assertThatThrownBy(() -> jwtHelper.extractUserIdFromToken("not-a-valid-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TOKEN_INVALID);
    }
}
