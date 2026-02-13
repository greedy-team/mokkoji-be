package com.greedy.mokkoji.user.service;

import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.dto.resopnse.LoginResponse;
import com.greedy.mokkoji.api.user.service.TokenService;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.RedisRepository;
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
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

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
    void generateToken() {
        // given
        final User expected = User.builder()
                .name("세종")
                .grade("4")
                .studentId("학번")
                .department("컴공과")
                .build();
        ReflectionTestUtils.setField(expected, "id", 1L);

        when(jwtUtil.generateAccessToken(expected.getId())).thenReturn("mockAccessToken");
        when(jwtUtil.generateRefreshToken(expected.getId())).thenReturn("mockRefreshToken");
        doNothing().when(redisRepository).save(anyString(), anyString(), anyLong());

        // when
        LoginResponse loginResponse = tokenService.generateToken(expected.getId());

        // then
        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.accessToken()).isNotBlank();
        assertThat(loginResponse.refreshToken()).isNotBlank();

        BDDMockito.verify(redisRepository, times(1))
                .save(eq("refreshToken" + expected.getId()), eq("mockRefreshToken"), anyLong());
    }

    @Test
    void deleteRefreshTokenWhenLogout() {
        // given
        final User user = User.builder()
                .name("세종")
                .grade("4")
                .studentId("학번")
                .department("컴공과")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        Long userId = user.getId();

        when(jwtUtil.generateAccessToken(userId)).thenReturn("mockAccessToken");
        when(jwtUtil.generateRefreshToken(userId)).thenReturn("mockRefreshToken");
        doNothing().when(redisRepository).save(anyString(), anyString(), anyLong());

        tokenService.generateToken(userId);

        //when
        tokenService.deleteRefreshToken(userId);

        //then
        assertThat(tokenService.getRefreshToken(user.getId())).isNull();

        BDDMockito.verify(redisRepository, times(1))
                .save(eq("refreshToken" + userId), eq("mockRefreshToken"), anyLong());
        BDDMockito.verify(redisRepository, times(1))
                .delete("refreshToken" + userId);
    }
}
