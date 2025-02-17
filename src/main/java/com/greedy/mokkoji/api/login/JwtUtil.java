package com.greedy.mokkoji.api.login;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // 비밀 키를 설정 파일에서 가져오기
    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;
    private static final long EXPIRATION_TIME = 86400000; // 1일

    @PostConstruct
    public void init() {
        if (secretKey == null) {
            throw new IllegalStateException("SECRET_KEY가 초기화되지 않았습니다.");
        }
        key = new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS256.getJcaName());
    }

    // userId를 기반으로 JWT 생성
    public String generateToken(String studentId) {
        return Jwts.builder()
                .setSubject(String.valueOf(studentId)) // userId를 주제(subject)로 설정
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256) // 서명 알고리즘과 비밀 키 설정
                .compact();
    }

    // 토큰에서 userId 추출
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject()); // Subject에 저장된 userId를 가져옴
    }
}

