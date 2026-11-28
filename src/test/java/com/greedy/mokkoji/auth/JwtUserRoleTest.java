package com.greedy.mokkoji.auth;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayName("JWT userRole 클레임")
class JwtUserRoleTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "test-secret-key-for-jwt-must-be-long-enough-256bit");
        jwtUtil.init();
    }

    @Test
    void access_토큰에_담긴_userRole을_그대로_읽어온다() {
        final String token = jwtUtil.generateAccessToken(
                new AuthCredential(AuthRole.USER, 1L, UserRole.CLUB_MASTER));

        final AuthCredential parsed = jwtUtil.getCredentialFromToken(token);

        assertThat(parsed.userRole()).isEqualTo(UserRole.CLUB_MASTER);
        assertThat(parsed.authRole()).isEqualTo(AuthRole.USER);
        assertThat(parsed.accountId()).isEqualTo(1L);
    }

    @Test
    void userRole이_없는_기존_토큰도_파싱된다() {
        final String token = jwtUtil.generateAccessToken(new AuthCredential(AuthRole.USER, 1L));

        final AuthCredential parsed = jwtUtil.getCredentialFromToken(token);

        assertThat(parsed.userRole()).isNull();
        assertThat(parsed.accountId()).isEqualTo(1L);
    }

    @Test
    void refresh_토큰에는_userRole을_담지_않는다() {
        final String token = jwtUtil.generateRefreshToken(
                new AuthCredential(AuthRole.USER, 1L, UserRole.CLUB_MASTER));

        final AuthCredential parsed = jwtUtil.getCredentialFromToken(token);

        assertThat(parsed.userRole()).isNull();
    }
}
