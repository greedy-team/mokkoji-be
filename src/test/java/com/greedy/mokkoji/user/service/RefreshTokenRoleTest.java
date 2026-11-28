package com.greedy.mokkoji.user.service;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.service.TokenService;
import com.greedy.mokkoji.api.user.service.UserService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("토큰 갱신 시 권한 반영")
class RefreshTokenRoleTest {

    private static final Long USER_ID = 1L;
    private static final String REFRESH_TOKEN = "refresh-token";

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtUtil jwtUtil;

    private void givenValidRefreshToken(final UserRole roleInToken) {
        given(jwtUtil.getCredentialFromToken(REFRESH_TOKEN))
                .willReturn(new AuthCredential(AuthRole.USER, USER_ID, roleInToken));
        given(tokenService.getRefreshToken(AuthRole.USER, USER_ID))
                .willReturn(REFRESH_TOKEN);
    }

    private User userWithRole(final UserRole role) {
        final University university = Fixture.createUniversity();
        return Fixture.createUserWithRole(university, role);
    }

    @Test
    void 권한이_바뀌면_토큰에_박힌_옛_권한이_아니라_DB의_현재_권한으로_재발급한다() {
        // given: 토큰에는 NORMAL 이 박혀 있지만 DB 에서는 이미 CLUB_MASTER 로 승격됨
        givenValidRefreshToken(UserRole.NORMAL);
        given(userRepository.findById(USER_ID))
                .willReturn(Optional.of(userWithRole(UserRole.CLUB_MASTER)));

        // when
        userService.refreshAccessToken(REFRESH_TOKEN);

        // then
        final ArgumentCaptor<AuthCredential> captor = ArgumentCaptor.forClass(AuthCredential.class);
        verify(jwtUtil).generateAccessToken(captor.capture());
        assertThat(captor.getValue().userRole()).isEqualTo(UserRole.CLUB_MASTER);
    }

    @Test
    void 권한이_강등된_경우도_DB의_현재_권한을_따른다() {
        // given: 토큰에는 CLUB_MASTER, DB 에서는 NORMAL 로 강등됨
        givenValidRefreshToken(UserRole.CLUB_MASTER);
        given(userRepository.findById(USER_ID))
                .willReturn(Optional.of(userWithRole(UserRole.NORMAL)));

        // when
        userService.refreshAccessToken(REFRESH_TOKEN);

        // then
        final ArgumentCaptor<AuthCredential> captor = ArgumentCaptor.forClass(AuthCredential.class);
        verify(jwtUtil).generateAccessToken(captor.capture());
        assertThat(captor.getValue().userRole()).isEqualTo(UserRole.NORMAL);
    }

    @Test
    void 존재하지_않는_사용자의_갱신은_예외를_던진다() {
        givenValidRefreshToken(UserRole.NORMAL);
        given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.refreshAccessToken(REFRESH_TOKEN))
                .isInstanceOf(MokkojiException.class);
    }

    @Test
    void 저장된_refresh_토큰과_다르면_예외를_던진다() {
        given(jwtUtil.getCredentialFromToken(REFRESH_TOKEN))
                .willReturn(new AuthCredential(AuthRole.USER, USER_ID, UserRole.NORMAL));
        given(tokenService.getRefreshToken(AuthRole.USER, USER_ID))
                .willReturn("different-token");

        assertThatThrownBy(() -> userService.refreshAccessToken(REFRESH_TOKEN))
                .isInstanceOf(MokkojiException.class);
        verify(jwtUtil, org.mockito.Mockito.never()).generateAccessToken(any());
    }
}
