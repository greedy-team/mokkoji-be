package com.greedy.mokkoji.api.jwt;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60; // 1시간
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 14; //14일

    private static final String AUTH_ROLE_KEY = "authRole";

    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;

    @PostConstruct
    public void init() {
        if (secretKey == null) {
            throw new MokkojiException(FailMessage.INTERNAL_TOKEN_INIT_ERROR);
        }
        key = new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS256.getJcaName());
    }

    public String generateAccessToken(AuthCredential credential) {
        return Jwts.builder()
                .setSubject(String.valueOf(credential.userId()))
                .claim(AUTH_ROLE_KEY, credential.authRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(AuthCredential credential) {
        return Jwts.builder()
                .setSubject(String.valueOf(credential.userId()))
                .claim(AUTH_ROLE_KEY, credential.authRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public AuthCredential getCredentialFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = Long.parseLong(claims.getSubject());
            AuthRole authRole = AuthRole.valueOf(claims.get(AUTH_ROLE_KEY, String.class));

            return new AuthCredential(authRole, userId);

        } catch (ExpiredJwtException e) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED_INVALID_TOKEN);
        }
    }
}
