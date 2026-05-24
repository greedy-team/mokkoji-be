package com.greedy.mokkoji.admin.service;

import com.greedy.mokkoji.api.admin.dto.response.AdminLoginResponse;
import com.greedy.mokkoji.api.admin.service.AdminAuthService;
import com.greedy.mokkoji.api.auth.dto.TokenPair;
import com.greedy.mokkoji.api.user.service.TokenService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.admin.entity.Admin;
import com.greedy.mokkoji.db.admin.repository.AdminRepository;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 로그인 서비스 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AdminAuthServiceTest {

    @InjectMocks
    private AdminAuthService adminAuthService;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Test
    @DisplayName("아이디와 비밀번호가 일치하면 토큰을 발급한다.")
    void login_success() {
        //given
        final String rawPassword = "rawPassword123!";
        final Admin admin = Fixture.createAdmin();
        ReflectionTestUtils.setField(admin, "id", 1L);

        given(adminRepository.findByLoginId(admin.getLoginId())).willReturn(Optional.of(admin));
        given(passwordEncoder.matches(rawPassword, admin.getPassword())).willReturn(true);
        given(tokenService.issueTokens(AuthRole.ADMIN, 1L)).willReturn(new TokenPair("access", "refresh"));

        //when
        final AdminLoginResponse response = adminAuthService.login(admin.getLoginId(), rawPassword);

        //then
        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        verify(tokenService, times(1)).issueTokens(AuthRole.ADMIN, 1L);
    }

    @Test
    @DisplayName("등록되지 않은 계정이면 NOT_FOUND 예외를 던진다.")
    void login_notFoundEmail() {
        //given
        final String loginId = "unknown@konkuk.ac.kr";
        given(adminRepository.findByLoginId(loginId)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> adminAuthService.login(loginId, "rawPassword123!"))
                .isInstanceOf(MokkojiException.class)
                .hasMessage(FailMessage.NOT_FOUND_ADMIN.getMessage());

        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 UNAUTHORIZED 예외를 던진다.")
    void login_wrongPassword() {
        //given
        final String rawPassword = "wrongPassword";
        final Admin admin = Fixture.createAdmin();
        ReflectionTestUtils.setField(admin, "id", 1L);

        given(adminRepository.findByLoginId(admin.getLoginId())).willReturn(Optional.of(admin));
        given(passwordEncoder.matches(rawPassword, admin.getPassword())).willReturn(false);

        //when & then
        assertThatThrownBy(() -> adminAuthService.login(admin.getLoginId(), rawPassword))
                .isInstanceOf(MokkojiException.class)
                .hasMessage(FailMessage.UNAUTHORIZED_ADMIN_LOGIN.getMessage());

        verifyNoInteractions(tokenService);
    }
}
