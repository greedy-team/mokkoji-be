package com.greedy.mokkoji.user.service;

import com.greedy.mokkoji.api.external.sejong.SejongLoginRestClient;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.dto.resopnse.StudentInformationResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.UserManageClubResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.UserManageClubsResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.UserRoleResponse;
import com.greedy.mokkoji.api.user.service.TokenService;
import com.greedy.mokkoji.api.user.service.UserService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.user.UserRole;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    SejongLoginRestClient sejongLoginClient;

    @Mock
    JwtUtil jwtUtil;

    @Test
    @DisplayName("로그인을 할 수 있다.")
    void login() {
        //given
        final String studentId = "학번";
        final String password = "비밀번호";

        final User expectedUser = User.builder()
                .name("세종")
                .grade("4")
                .studentId("학번")
                .department("컴공과")
                .build();

        BDDMockito.given(userRepository.findByStudentId(any())).willReturn(Optional.ofNullable(expectedUser));

        //when
        final User actualUser = userService.login(studentId, password);

        //then
        Assertions.assertThat(actualUser).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("처음 로그인 시 새로운 User로 등록된다.")
    void SaveUserWhenFirstLogin() {
        // given
        String studentId = "학번";
        String password = "비밀번호";

        StudentInformationResponse studentInfo = StudentInformationResponse.of("세종", "컴공과", "4");
        User expectedUser = User.builder()
                .name("세종")
                .grade("4")
                .studentId(studentId)
                .department("컴공과")
                .role(UserRole.NORMAL)
                .build();

        BDDMockito.given(sejongLoginClient.getStudentInformation(any(), any())).willReturn(studentInfo);
        BDDMockito.given(userRepository.findByStudentId(any())).willReturn(Optional.empty());
        BDDMockito.given(userRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        User newUser = userService.login(studentId, password);

        // then
        Assertions.assertThat(newUser).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("이미 등록된 사용자가 로그인 시 기존 User 객체가 반환된다.")
    void ReturnUserWhenNotFirstLogin() {
        // given
        String studentId = "학번";
        String password = "비밀번호";

        User existingUser = User.builder()
                .name("세종")
                .grade("4")
                .studentId(studentId)
                .department("컴공과")
                .build();

        BDDMockito.given(userRepository.findByStudentId(any())).willReturn(Optional.of(existingUser));

        // when
        User returnedUser = userService.login(studentId, password);

        // then
        Assertions.assertThat(returnedUser).usingRecursiveComparison().isEqualTo(existingUser);
        BDDMockito.verify(userRepository, BDDMockito.never()).save(any());
    }

    @Test
    @DisplayName("AccessToken을 재발급 받을 수 있다.")
    void refreshAccessToken() {
        // given
        Long userId = 1L;
        String refreshToken = "refreshToken";
        String newAccessToken = "newAccessToken";

        when(jwtUtil.getUserIdFromToken(refreshToken)).thenReturn(userId);
        when(tokenService.getRefreshToken(userId)).thenReturn(refreshToken);
        when(jwtUtil.generateAccessToken(userId)).thenReturn(newAccessToken);

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

        when(jwtUtil.getUserIdFromToken(invalidRefreshToken)).thenReturn(userId);
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
                .grade("4")
                .studentId("학번")
                .department("컴공과")
                .email("origin@email.com")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // when
        userService.updateEmail(1L, "updated@email.com");

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
            .grade("4")
            .studentId("학번")
            .department("컴공과")
            .role(UserRole.GREEDY_ADMIN)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        UserRoleResponse actualRole = userService.getUserRole(userId);

        // then
        assertThat(actualRole.role()).isEqualTo(UserRole.GREEDY_ADMIN.toString());
    }

    @Test
    @DisplayName("사용자가 회장으로 관리 중인 동아리를 조회할 수 있다.")
    void getUserManageClubs() {
        // given
        Long userId = 1L;

        User user = User.builder()
            .name("모꼬지")
            .studentId("12341234")
            .grade("4")
            .department("컴퓨터공학과")
            .email("모꼬지@test.com")
            .role(UserRole.GREEDY_ADMIN)
            .build();

        Club club1 = Club.builder()
            .name("그리디1")
            .clubMasterStudentId("12341234")
            .build();

        Club club2 = Club.builder()
            .name("그리디2")
            .clubMasterStudentId("12341234")
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(clubRepository.findByClubMasterStudentId(user.getStudentId())).thenReturn(List.of(club1, club2));

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
