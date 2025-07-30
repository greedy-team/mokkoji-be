package com.greedy.mokkoji.user.service;

import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.service.TokenService;
import com.greedy.mokkoji.api.user.service.UserService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
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
    SejongLoginClient sejongLoginClient;

    @Mock
    JwtUtil jwtUtil;

    @Test
    void 로그인을_할_수_있다() {
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
    @DisplayName("이미 등록된 사용자가 로그인하면 기존 User객체가 반환된다.")
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
    @DisplayName("access토큰을 재발급 받을 수 있다.")
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
    @DisplayName("access토큰 재발급 시 잘못된 refresh토큰이면 예외가 발생한다.")
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
    @DisplayName("User정보를 업데이트 할 수 있다.")
    void UpdateUser() {
        // given
        final User user = User.builder()
                .name("세종")
                .grade("4")
                .studentId("학번")
                .department("컴공과")
                .email("a@email.com")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // when
        userService.updateEmail(1L, "b@email.com");

        // then
        assertThat(user.getEmail()).isEqualTo("b@email.com");
    }
}
