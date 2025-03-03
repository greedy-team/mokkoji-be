package com.greedy.mokkoji.user.service;

import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.dto.resopnse.LoginResponse;
import com.greedy.mokkoji.api.user.service.TokenService;
import com.greedy.mokkoji.db.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
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
    StringRedisTemplate redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @Test
    void Login시_토큰을_발급_받을_수_있다() {
        // given
        final User expected = User.builder()
                .name("세종")
                .grade("4")
                .studentId("학번")
                .department("컴공과")
                .build();

        when(jwtUtil.generateAccessToken(expected.getId())).thenReturn("mockAccessToken");
        when(jwtUtil.generateRefreshToken(expected.getId())).thenReturn("mockRefreshToken");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any());

        // when
        LoginResponse loginResponse = tokenService.generateToken(expected.getId());

        // then
        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.accessToken()).isNotBlank();
        assertThat(loginResponse.refreshToken()).isNotBlank();
    }

    @Test
    void Logout시_refresh토큰을_없앨_수_있다() {
        // given
        final User user = User.builder()
                .name("세종")
                .grade("4")
                .studentId("학번")
                .department("컴공과")
                .build();

        Long userId = user.getId();

        when(jwtUtil.generateAccessToken(userId)).thenReturn("mockAccessToken");
        when(jwtUtil.generateRefreshToken(userId)).thenReturn("mockRefreshToken");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any());

        tokenService.generateToken(userId);

        //when
        tokenService.deleteRefreshToken(userId);

        //then
        assertThat(tokenService.getRefreshToken(user.getId())).isNull();
    }
}
