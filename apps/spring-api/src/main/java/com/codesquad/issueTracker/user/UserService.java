package com.codesquad.issueTracker.user;

import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.security.PasswordHelper;
import com.codesquad.issueTracker.user.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public void handleSignupRequest(SignupRequest request){
        String username = request.username();
        String hashedPassword = PasswordHelper.hashPassword(request.password());

        if(repository.existsUserByUsername(username)){
            throw new BusinessException(ErrorCode.USERNAME_TAKEN);
        }
        else{
        User newUser = new User(null, username,hashedPassword,null,null,null);
        repository.save(newUser);
        }
    }
}
