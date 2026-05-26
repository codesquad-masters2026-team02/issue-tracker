package com.codesquad.issueTracker.auth;

import com.codesquad.issueTracker.auth.dto.GithubTokenResponse;
import com.codesquad.issueTracker.auth.dto.GithubUserResponse;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GithubOAuthClient {

    private final RestClient restClient;

    @Value("${github.client-id}")
    private String clientId;

    @Value("${github.client-secret}")
    private String clientSecret;

    public GithubOAuthClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public GithubTokenResponse getAccessToken(String code) {

        return restClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "code", code
                ))
                .retrieve()
                .body(GithubTokenResponse.class);
    }

    public GithubUserResponse getUser(String accessToken) {

        return restClient.get()
                .uri("https://api.github.com/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(GithubUserResponse.class);
    }

}
