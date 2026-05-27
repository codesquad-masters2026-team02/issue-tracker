package com.codesquad.issueTracker.user;

import com.codesquad.issueTracker.attachment.AttachmentService;
import com.codesquad.issueTracker.auth.GithubOAuthClient;
import com.codesquad.issueTracker.auth.OAuthProvider;
import com.codesquad.issueTracker.auth.dto.GithubTokenResponse;
import com.codesquad.issueTracker.auth.dto.GithubUserResponse;
import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.security.PasswordHelper;
import com.codesquad.issueTracker.security.JwtHelper;
import com.codesquad.issueTracker.user.dto.LoginRequest;
import com.codesquad.issueTracker.user.dto.ProfileEditRequest;
import com.codesquad.issueTracker.user.dto.SignupRequest;
import com.codesquad.issueTracker.user.dto.TokenResponse;
import com.codesquad.issueTracker.user.dto.UserInfoResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final JwtHelper jwtHelper;
    private final GithubOAuthClient githubOAuthClient;
    private final AttachmentService attachmentService;

    public void handleSignupRequest(SignupRequest request) {
        String username = request.username();
        String hashedPassword = PasswordHelper.hashPassword(request.password());

        if (repository.existsUserByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_TAKEN);
        } else {
            User newUser = new User(null, username, hashedPassword, null, null, null, null);
            repository.save(newUser);
        }
    }

    @Transactional
    public TokenResponse login(String code) {
        GithubTokenResponse tokenResponse = githubOAuthClient.getAccessToken(code);
        GithubUserResponse githubUser = githubOAuthClient.getUser(tokenResponse.accessToken());

        Optional<User> findUser = repository.findUserByOauthProviderAndOauthId(
                OAuthProvider.GITHUB, githubUser.id());

        User user = findUser.orElseGet(() -> {
            String image = githubUser.avatarUrl();
            Long oauthId = githubUser.id();
            String name = githubUser.name();

            return repository.save(
                    new User(null, name, null, null,
                            OAuthProvider.GITHUB, String.valueOf(oauthId), image
                    )
            );
        });

        String accessToken = jwtHelper.createUserAccessToken(user.getId());
        String refreshToken = jwtHelper.createUserRefreshToken(user.getId());
        repository.updateRefreshToken(user.getId(), refreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse handleLoginRequest(LoginRequest request) {
        String username = request.username();
        String password = request.password();

        if (!repository.existsUserByUsername(username)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        } else {
            User possibleUser = repository.findUserByUsername(username);
            if (!PasswordHelper.verifyPassword(password, possibleUser.getPassword())) {
                throw new BusinessException(ErrorCode.USER_INFO_NOT_MATCHED);
            }
            String accessToken = jwtHelper.createUserAccessToken(possibleUser.getId());
            String refreshToken = jwtHelper.createUserRefreshToken(possibleUser.getId());

            repository.updateRefreshToken(possibleUser.getId(), refreshToken);

            return new TokenResponse(accessToken, refreshToken);
        }
    }

    public TokenResponse refreshAccessToken(String refreshToken) {
        long requestedUserId = jwtHelper.extractUserIdFromToken(refreshToken);
        User user = findById(requestedUserId);
        if (!refreshToken.equals(user.getRefresh_token())) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        String accessToken = jwtHelper.createUserAccessToken(requestedUserId);
        return new TokenResponse(accessToken, null);
    }

    public UserInfoResponse findUserInfo(Long userId) {
        User user = findById(userId);
        return UserInfoResponse.from(user);
    }

    public void handleLogoutRequest(Long userId) {
        repository.wipeRefreshToken(userId);
    }

    public List<UserInfoResponse> findAllUsers() {
        return repository.findAll().stream().map(UserInfoResponse::from).toList();
    }

    public void editProfile(ProfileEditRequest request, Long userId) {
        User user = findById(userId);
        String oldImage = user.getProfileImageUrl();

        user.editProfile(request.imageUrl());
        repository.save(user);

        if (oldImage != null) {
            attachmentService.removeOld(oldImage);
        }
    }

    private User findById(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
