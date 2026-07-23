package com.greedy.mokkoji.user.service;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.dto.TokenPair;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.service.TokenService;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.RedisRepository;
import com.greedy.mokkoji.enums.auth.AuthRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("토큰 서비스 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TokenServiceTest {

    @InjectMocks
    TokenService tokenService;

    @Mock
    JwtUtil jwtUtil;

    @Mock
    RedisRepository redisRepository;

    @Test
    @DisplayName("로그인 시 토큰을 발급 받을 수 있다")
    void issueTokens() {
        // given
        final User expected = User.builder()
                .name("세종")
                .kakaoId("kakao-12341234")
                .build();
        ReflectionTestUtils.setField(expected, "id", 1L);

        when(jwtUtil.generateAccessToken(any(AuthCredential.class))).thenReturn("mockAccessToken");
        when(jwtUtil.generateRefreshToken(any(AuthCredential.class))).thenReturn("mockRefreshToken");
        doNothing().when(redisRepository).save(anyString(), anyString(), anyLong());

        // when
        TokenPair tokenPair = tokenService.issueTokens(AuthRole.USER, expected.getId());

        // then
        assertThat(tokenPair).isNotNull();
        assertThat(tokenPair.accessToken()).isNotBlank();
        assertThat(tokenPair.refreshToken()).isNotBlank();

        BDDMockito.verify(redisRepository, times(1))
                .save(eq("refreshToken:USER:" + expected.getId()), eq("mockRefreshToken"), anyLong());
    }

    @Test
    void deleteRefreshTokenWhenLogout() {
        // given
        final User user = User.builder()
                .name("세종")
                .kakaoId("kakao-12341234")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        Long userId = user.getId();

        when(jwtUtil.generateAccessToken(any(AuthCredential.class))).thenReturn("mockAccessToken");
        when(jwtUtil.generateRefreshToken(any(AuthCredential.class))).thenReturn("mockRefreshToken");
        doNothing().when(redisRepository).save(anyString(), anyString(), anyLong());

        tokenService.issueTokens(AuthRole.USER, userId);

        //when
        tokenService.deleteRefreshToken(AuthRole.USER, userId);

        //then
        assertThat(tokenService.getRefreshToken(AuthRole.USER, user.getId())).isNull();

        BDDMockito.verify(redisRepository, times(1))
                .save(eq("refreshToken:USER:" + userId), eq("mockRefreshToken"), anyLong());
        BDDMockito.verify(redisRepository, times(1))
                .delete("refreshToken:USER:" + userId);
    }
}
