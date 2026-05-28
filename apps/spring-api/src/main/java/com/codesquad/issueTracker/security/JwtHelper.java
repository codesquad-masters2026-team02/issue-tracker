package com.codesquad.issueTracker.security;

import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtHelper {

    private final long accessTokenExpInMs;
    private final long refreshTokenExpInMs;
    private final SecretKey secretKey ;


    public JwtHelper(
            @Value("${jwt.secret-key}") String secretKeyString,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpInMs,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpInMs) {

        this.accessTokenExpInMs = accessTokenExpInMs;
        this.refreshTokenExpInMs = refreshTokenExpInMs;
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }


    public String createUserAccessToken(long userId){
        return createUserToken(userId, accessTokenExpInMs);
    }

    public String createUserRefreshToken(long userId){
        return createUserToken(userId, refreshTokenExpInMs);
    }

    private String createUserToken(Long userId, long expInMs){
        Date now = new Date();
        Date expDate = new Date(now.getTime() + expInMs);
        return Jwts.builder().subject(String.valueOf(userId)).issuedAt(now).expiration(expDate).signWith(secretKey).compact();
    }

    public long extractUserIdFromToken(String token){
        try{
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            return Long.parseLong(claims.getSubject());
        }
        catch(JwtException e){
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }
}
