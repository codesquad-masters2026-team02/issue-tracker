package com.codesquad.issueTracker.common.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.security.JwtHelper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AuthInterceptorTest {

    private final JwtHelper jwtHelper = new JwtHelper(
            "ThisIsALocalSecretKeyForTestingAndThisWillBeReplacedWithSecretKeyInGithubAction",
            3_600_000,
            1_209_600_000
    );
    private final AuthInterceptor interceptor = new AuthInterceptor(jwtHelper);

    @Test
    void optionsRequestBypassesAuth() {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/issues");
        HttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
    }

    @Test
    void missingAuthorizationHeaderThrowsLoginRequired() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/issues");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOGIN_REQUIRED);
    }

    @Test
    void invalidAuthorizationHeaderThrowsLoginRequired() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/issues");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Token abc");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOGIN_REQUIRED);
    }

    @Test
    void validBearerTokenStoresUserIdRequestAttribute() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/issues");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwtHelper.createUserAccessToken(123L));

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        assertThat(request.getAttribute("userId")).isEqualTo(123L);
    }
}
