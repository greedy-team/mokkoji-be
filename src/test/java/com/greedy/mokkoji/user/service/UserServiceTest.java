package com.greedy.mokkoji.user.service;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.dto.resopnse.LoginResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.UserManageClubResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.UserManageClubsResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.UserRoleResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.kakao.KakaoUserInfoResponse;
import com.greedy.mokkoji.api.user.service.TokenService;
import com.greedy.mokkoji.api.user.service.UserService;
import com.greedy.mokkoji.api.user.service.kakao.KakaoSocialLoginService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.user.UserRole;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("유저 서비스 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    TokenService tokenService;

    @Mock
    UserRepository userRepository;

    @Mock
    ClubRepository clubRepository;

    @Mock
    KakaoSocialLoginService kakaoSocialLoginService;

    @Mock
    JwtUtil jwtUtil;

    @Test
    @DisplayName("기존 사용자가 카카오 로그인 시 기존 유저로 토큰을 발급한다.")
    void kakaoLoginExistingUser() {
        // given
        final String code = "authCode";
        final String kakaoId = "kakao-12341234";

        final User existingUser = User.builder()
                .name("세종")
                .kakaoId(kakaoId)
                .role(UserRole.NORMAL)
                .build();
        ReflectionTestUtils.setField(existingUser, "id", 1L);

        final LoginResponse expected = LoginResponse.of("accessToken", "refreshToken", false);

        BDDMockito.given(kakaoSocialLoginService.login(code))
                .willReturn(new KakaoUserInfoResponse(kakaoId));
        BDDMockito.given(userRepository.findByKakaoId(kakaoId)).willReturn(Optional.of(existingUser));
        BDDMockito.given(tokenService.generateToken(AuthRole.USER, existingUser.getId(), false))
                .willReturn(expected);

        // when
        final LoginResponse actual = userService.kakaoLogin(code);

        // then
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        BDDMockito.verify(userRepository, BDDMockito.never()).save(any());
    }

    @Test
    @DisplayName("처음 카카오 로그인 시 새로운 User로 등록되고 isNewUser가 true로 발급된다.")
    void kakaoLoginNewUser() {
        // given
        final String code = "authCode";
        final String kakaoId = "kakao-99999999";

        final LoginResponse expected = LoginResponse.of("accessToken", "refreshToken", true);

        BDDMockito.given(kakaoSocialLoginService.login(code))
                .willReturn(new KakaoUserInfoResponse(kakaoId));
        BDDMockito.given(userRepository.findByKakaoId(kakaoId)).willReturn(Optional.empty());
        BDDMockito.given(userRepository.save(any())).willAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });
        BDDMockito.given(tokenService.generateToken(AuthRole.USER, 1L, true)).willReturn(expected);

        // when
        final LoginResponse actual = userService.kakaoLogin(code);

        // then
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        BDDMockito.verify(userRepository, BDDMockito.times(1)).save(any());
    }

    @Test
    @DisplayName("AccessToken을 재발급 받을 수 있다.")
    void refreshAccessToken() {
        // given
        Long userId = 1L;
        String refreshToken = "refreshToken";
        String newAccessToken = "newAccessToken";
        AuthCredential credential = new AuthCredential(AuthRole.USER, userId);

        when(jwtUtil.getCredentialFromToken(refreshToken)).thenReturn(credential);
        when(tokenService.getRefreshToken(userId)).thenReturn(refreshToken);
        when(jwtUtil.generateAccessToken(credential)).thenReturn(newAccessToken);

        // when
        String accessToken = userService.refreshAccessToken(refreshToken);

        // then
        assertThat(accessToken).isEqualTo(newAccessToken);
    }

    @Test
    @DisplayName("AccessToken 재발급 시 잘못된 RefreshToken이면 예외가 발생한다.")
    void wrongRefreshTokenWhenRefreshAccessToken() {
        // given
        Long userId = 1L;
        String invalidRefreshToken = "invalidRefreshToken";
        AuthCredential credential = new AuthCredential(AuthRole.USER, userId);

        when(jwtUtil.getCredentialFromToken(invalidRefreshToken)).thenReturn(credential);
        when(tokenService.getRefreshToken(userId)).thenReturn("differentStoredToken");

        // when & then
        assertThatThrownBy(() -> userService.refreshAccessToken(invalidRefreshToken))
                .isInstanceOf(MokkojiException.class)
                .hasMessage(FailMessage.UNAUTHORIZED.getMessage());
    }

    @Test
    @DisplayName("User의 이메일 정보를 업데이트 할 수 있다.")
    void updateEmail() {
        // given
        final User user = User.builder()
                .name("세종")
                .kakaoId("kakao-12341234")
                .email("origin@email.com")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // when
        userService.updateUserInformation(1L, "updated@email.com", null);

        // then
        assertThat(user.getEmail()).isEqualTo("updated@email.com");
    }

    @Test
    @DisplayName("사용자의 역할을 조회할 수 있다.")
    void getUserRole() {
        // given
        Long userId = 1L;

        User user = User.builder()
                .name("세종")
                .kakaoId("kakao-12341234")
                .role(UserRole.CLUB_MASTER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        UserRoleResponse actualRole = userService.getUserRole(userId);

        // then
        assertThat(actualRole.role()).isEqualTo(UserRole.CLUB_MASTER);
    }

    @Test
    @DisplayName("사용자가 회장으로 관리 중인 동아리를 조회할 수 있다.")
    void getUserManageClubs() {
        // given
        Long userId = 1L;

        User user = User.builder()
                .name("모꼬지")
                .kakaoId("kakao-12341234")
                .email("모꼬지@test.com")
                .role(UserRole.CLUB_MASTER)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        Club club1 = Club.builder().name("그리디1").master(user).build();
        ReflectionTestUtils.setField(club1, "id", 1L);
        Club club2 = Club.builder().name("그리디2").master(user).build();
        ReflectionTestUtils.setField(club2, "id", 2L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(clubRepository.findByMasterId(userId)).thenReturn(List.of(club1, club2));

        UserManageClubsResponse expectedResponse = new UserManageClubsResponse(
                List.of(
                        new UserManageClubResponse(club1.getId(), club1.getName()),
                        new UserManageClubResponse(club2.getId(), club2.getName())
                )
        );

        // when
        UserManageClubsResponse actualResponse = userService.getUserManageClubs(userId);

        // then
        assertThat(actualResponse.clubs()).hasSize(2);
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }
}
