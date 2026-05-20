package com.codesquad.issueTracker.user;

import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.security.PasswordHelper;
import com.codesquad.issueTracker.security.JwtHelper;
import com.codesquad.issueTracker.user.dto.LoginRequest;
import com.codesquad.issueTracker.user.dto.SignupRequest;
import com.codesquad.issueTracker.user.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final JwtHelper jwtHelper;

    public void handleSignupRequest(SignupRequest request){
        String username = request.username();
        String hashedPassword = PasswordHelper.hashPassword(request.password());

        if(repository.existsUserByUsername(username)){
            throw new BusinessException(ErrorCode.USERNAME_TAKEN);
        }
        else{
        User newUser = new User(null, username,hashedPassword,null,null,null,null);
        repository.save(newUser);
        }
    }

    public TokenResponse handleLoginRequest(LoginRequest request){
        String username = request.username();
        String password = request.password();

        if(!repository.existsUserByUsername(username)){
            throw new BusinessException(ErrorCode.USERNAME_TAKEN);
        }
        else{
            User possibleUser = repository.findUserByUsername(username);
            if(!PasswordHelper.verifyPassword(password,possibleUser.getPassword())){
                throw new BusinessException(ErrorCode.USER_INFO_NOT_MATCHED);
            }
            String accessToken = jwtHelper.createUserAccessToken(possibleUser.getId());
            String refreshToken = jwtHelper.createUserRefreshToken(possibleUser.getId());

            repository.updateRefreshToken(possibleUser.getId(), refreshToken);

            return new TokenResponse(accessToken,refreshToken);
        }
    }

    public TokenResponse refreshAccessToken(String refreshToken){
        long requestedUserId = jwtHelper.extractUserIdFromToken(refreshToken);
        User user = repository.findById(requestedUserId).orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));
        if(!refreshToken.equals(user.getRefresh_token())){
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        String accessToken = jwtHelper.createUserAccessToken(requestedUserId);
        return new TokenResponse(accessToken, null);
    }
}
