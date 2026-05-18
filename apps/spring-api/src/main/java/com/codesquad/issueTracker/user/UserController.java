package com.codesquad.issueTracker.user;

import com.codesquad.issueTracker.user.dto.SignupRequest;
import com.codesquad.issueTracker.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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


}
