package com.greedy.mokkoji.auth.service;

import com.greedy.mokkoji.api.auth.service.ClubManageAuthorizer;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.admin.entity.Admin;
import com.greedy.mokkoji.db.admin.repository.AdminRepository;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.auth.AuthRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("동아리 관리 권한 검증 테스트")
@ExtendWith(MockitoExtension.class)
class ClubManageAuthorizerTest {

    @InjectMocks
    private ClubManageAuthorizer clubManageAuthorizer;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private Club club;

    @Test
    @DisplayName("모꼬지 관리자는 동아리를 관리할 수 있다.")
    void validateCanManageClubByMokkojiAdmin() {
        //given
        final Long adminId = 1L;
        final Admin admin = mock(Admin.class);

        BDDMockito.given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));
        BDDMockito.given(admin.canManageAnyClub()).willReturn(true);

        //when & then
        assertThatCode(() -> clubManageAuthorizer.validateCanManageClub(AuthRole.ADMIN, adminId, club))
                .doesNotThrowAnyException();

        verify(adminRepository, times(1)).findById(adminId);
        verify(admin, times(1)).canManageAnyClub();
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("관리 권한이 없는 관리자는 동아리를 관리할 수 없다.")
    void validateCanManageClubByAdminWithoutPermission() {
        //given
        final Long adminId = 1L;
        final Admin admin = mock(Admin.class);

        BDDMockito.given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));
        BDDMockito.given(admin.canManageAnyClub()).willReturn(false);

        //when & then
        assertThatThrownBy(() -> clubManageAuthorizer.validateCanManageClub(AuthRole.ADMIN, adminId, club))
                .isInstanceOf(MokkojiException.class);

        verify(adminRepository, times(1)).findById(adminId);
        verify(admin, times(1)).canManageAnyClub();
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("동아리장은 자신이 관리 중인 동아리를 관리할 수 있다.")
    void validateCanManageClubByClubMaster() {
        //given
        final Long userId = 1L;
        final User user = mock(User.class);

        BDDMockito.given(userRepository.findById(userId)).willReturn(Optional.of(user));
        BDDMockito.given(user.canManageClub(club)).willReturn(true);

        //when & then
        assertThatCode(() -> clubManageAuthorizer.validateCanManageClub(AuthRole.USER, userId, club))
                .doesNotThrowAnyException();

        verify(userRepository, times(1)).findById(userId);
        verify(user, times(1)).canManageClub(club);
        verifyNoInteractions(adminRepository);
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 동아리를 관리할 수 없다.")
    void validateCanManageClubByNotFoundUser() {
        //given
        final Long userId = 1L;

        BDDMockito.given(userRepository.findById(userId)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> clubManageAuthorizer.validateCanManageClub(AuthRole.USER, userId, club))
                .isInstanceOf(MokkojiException.class);

        verify(userRepository, times(1)).findById(userId);
        verifyNoInteractions(adminRepository);
    }

    @Test
    @DisplayName("동아리장이 아닌 사용자는 동아리를 관리할 수 없다.")
    void validateCanManageClubByUserWithoutPermission() {
        //given
        final Long userId = 1L;
        final User user = mock(User.class);

        BDDMockito.given(userRepository.findById(userId)).willReturn(Optional.of(user));
        BDDMockito.given(user.canManageClub(club)).willReturn(false);

        //when & then
        assertThatThrownBy(() -> clubManageAuthorizer.validateCanManageClub(AuthRole.USER, userId, club))
                .isInstanceOf(MokkojiException.class);

        verify(userRepository, times(1)).findById(userId);
        verify(user, times(1)).canManageClub(club);
        verifyNoInteractions(adminRepository);
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 동아리를 관리할 수 없다.")
    void validateCanManageClubByUnauthenticatedUser() {
        //given
        final Long userId = null;

        //when & then
        assertThatThrownBy(() -> clubManageAuthorizer.validateCanManageClub(AuthRole.USER, userId, club))
                .isInstanceOf(MokkojiException.class);

        verifyNoInteractions(userRepository);
        verifyNoInteractions(adminRepository);
    }
}
