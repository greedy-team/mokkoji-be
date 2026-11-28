package com.greedy.mokkoji.api.jwt;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.user.UserRole;
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
    public static final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 30; // 30분
    public static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 14; //14일

    private static final String AUTH_ROLE_KEY = "authRole";
    private static final String USER_ROLE_KEY = "userRole";

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
        final JwtBuilder builder = Jwts.builder()
                .setSubject(String.valueOf(credential.accountId()))
                .claim(AUTH_ROLE_KEY, credential.authRole().name());

        if (credential.userRole() != null) {
            builder.claim(USER_ROLE_KEY, credential.userRole().name());
        }

        return builder
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(AuthCredential credential) {
        return Jwts.builder()
                .setSubject(String.valueOf(credential.accountId()))
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

            Long accountId = Long.parseLong(claims.getSubject());
            AuthRole authRole = AuthRole.valueOf(claims.get(AUTH_ROLE_KEY, String.class));
            UserRole userRole = parseUserRole(claims.get(USER_ROLE_KEY, String.class));

            return new AuthCredential(authRole, accountId, userRole);

        } catch (ExpiredJwtException e) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED_INVALID_TOKEN);
        }
    }

    private UserRole parseUserRole(final String value) {
        if (value == null) {
            return null;
        }
        try {
            return UserRole.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
