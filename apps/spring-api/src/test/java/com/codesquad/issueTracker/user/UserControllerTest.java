package com.codesquad.issueTracker.user;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codesquad.issueTracker.security.JwtHelper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    private static final String PASSWORD = "Password1!";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtHelper jwtHelper;

    @Test
    void signupAndSigninReturnExpectedAuthContract() throws Exception {
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"api-user","password":"Password1!"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/users/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"api-user","password":"Password1!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void refreshUsesRefreshTokenCookie() throws Exception {
        signUp("refresh-api-user");
        MvcResult signIn = mockMvc.perform(post("/api/users/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"refresh-api-user","password":"Password1!"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        Cookie refreshToken = signIn.getResponse().getCookie("refreshToken");

        mockMvc.perform(post("/api/users/refresh").cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void meAndLogoutRequireValidAccessToken() throws Exception {
        signUp("me-api-user");
        User user = userRepository.findUserByUsername("me-api-user");
        String bearer = "Bearer " + jwtHelper.createUserAccessToken(user.getId());

        mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(user.getId()))
                .andExpect(jsonPath("$.data.username").value("me-api-user"));

        mockMvc.perform(post("/api/users/logout").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("refreshToken", 0))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void protectedUserListRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("LOGIN_REQUIRED"));
    }

    private void signUp(String username) throws Exception {
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, PASSWORD)))
                .andExpect(status().isCreated());
    }
}
