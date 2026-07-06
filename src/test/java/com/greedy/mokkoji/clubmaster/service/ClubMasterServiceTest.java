package com.greedy.mokkoji.clubmaster.service;

import com.greedy.mokkoji.api.auth.service.ManageAuthorizer;
import com.greedy.mokkoji.api.clubmaster.service.ClubMasterService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.clubmaster.repository.ClubMasterApplicationRepository;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.university.repository.UniversityRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
@DisplayName("동아리장 권한 위임 서비스 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class ClubMasterServiceTest {

    private static final Long CLUB_ID = 10L;
    private static final Long PREVIOUS_MASTER_ID = 1L;
    private static final Long NEXT_MASTER_ID = 2L;
    private static final String NEXT_MASTER_USER_CODE = "next-user-code";

    @InjectMocks
    ClubMasterService clubMasterService;

    @Mock
    ClubMasterApplicationRepository clubMasterApplicationRepository;

    @Mock
    ManageAuthorizer manageAuthorizer;

    @Mock
    UniversityRepository universityRepository;

    @Mock
    ClubRepository clubRepository;

    @Mock
    UserRepository userRepository;

    private User previousMaster;
    private User nextMaster;
    private Club club;

    private void prepareTransferContext(final User master) {
        final University university = Fixture.createUniversity();

        nextMaster = Fixture.createAnotherUser();
        ReflectionTestUtils.setField(nextMaster, "id", NEXT_MASTER_ID);
        ReflectionTestUtils.setField(nextMaster, "code", NEXT_MASTER_USER_CODE);

        club = Fixture.createClub(university, master);
        ReflectionTestUtils.setField(club, "id", CLUB_ID);
    }

    @Test
    @DisplayName("동아리장이 대상자의 user_code를 입력하면 즉시 권한이 위임된다.")
    void transferClubMaster() {
        // given
        previousMaster = Fixture.createUserWithRole(UserRole.CLUB_MASTER);
        ReflectionTestUtils.setField(previousMaster, "id", PREVIOUS_MASTER_ID);
        prepareTransferContext(previousMaster);

        BDDMockito.given(clubRepository.findById(CLUB_ID)).willReturn(Optional.of(club));
        BDDMockito.given(userRepository.findByCode(NEXT_MASTER_USER_CODE)).willReturn(Optional.of(nextMaster));
        BDDMockito.given(clubRepository.existsByMaster_Id(PREVIOUS_MASTER_ID)).willReturn(false);

        // when
        clubMasterService.transferClubMaster(AuthRole.USER, PREVIOUS_MASTER_ID, CLUB_ID, NEXT_MASTER_USER_CODE);

        // then
        assertThat(nextMaster.getRole()).isEqualTo(UserRole.CLUB_MASTER);
        assertThat(club.getMaster()).isEqualTo(nextMaster);
        assertThat(previousMaster.getRole()).isEqualTo(UserRole.NORMAL);
    }

    @Test
    @DisplayName("기존 동아리장이 다른 동아리의 동아리장이면 권한이 유지된다.")
    void transferClubMasterKeepsPreviousMasterRole() {
        // given
        previousMaster = Fixture.createUserWithRole(UserRole.CLUB_MASTER);
        ReflectionTestUtils.setField(previousMaster, "id", PREVIOUS_MASTER_ID);
        prepareTransferContext(previousMaster);

        BDDMockito.given(clubRepository.findById(CLUB_ID)).willReturn(Optional.of(club));
        BDDMockito.given(userRepository.findByCode(NEXT_MASTER_USER_CODE)).willReturn(Optional.of(nextMaster));
        BDDMockito.given(clubRepository.existsByMaster_Id(PREVIOUS_MASTER_ID)).willReturn(true);

        // when
        clubMasterService.transferClubMaster(AuthRole.USER, PREVIOUS_MASTER_ID, CLUB_ID, NEXT_MASTER_USER_CODE);

        // then
        assertThat(nextMaster.getRole()).isEqualTo(UserRole.CLUB_MASTER);
        assertThat(club.getMaster()).isEqualTo(nextMaster);
        assertThat(previousMaster.getRole()).isEqualTo(UserRole.CLUB_MASTER);
    }

    @Test
    @DisplayName("존재하지 않는 동아리로 위임을 시도하면 예외가 발생한다.")
    void transferClubMasterClubNotFound() {
        // given
        BDDMockito.given(clubRepository.findById(CLUB_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubMasterService.transferClubMaster(AuthRole.USER, PREVIOUS_MASTER_ID, CLUB_ID, NEXT_MASTER_USER_CODE))
                .isInstanceOf(MokkojiException.class)
                .hasMessage(FailMessage.NOT_FOUND_CLUB.getMessage());

        BDDMockito.verify(userRepository, BDDMockito.never()).findByCode(anyString());
    }

    @Test
    @DisplayName("존재하지 않는 user_code로 위임을 시도하면 예외가 발생한다.")
    void transferClubMasterUserCodeNotFound() {
        // given
        previousMaster = Fixture.createUserWithRole(UserRole.CLUB_MASTER);
        ReflectionTestUtils.setField(previousMaster, "id", PREVIOUS_MASTER_ID);
        prepareTransferContext(previousMaster);

        BDDMockito.given(clubRepository.findById(CLUB_ID)).willReturn(Optional.of(club));
        BDDMockito.given(userRepository.findByCode(NEXT_MASTER_USER_CODE)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubMasterService.transferClubMaster(AuthRole.USER, PREVIOUS_MASTER_ID, CLUB_ID, NEXT_MASTER_USER_CODE))
                .isInstanceOf(MokkojiException.class)
                .hasMessage(FailMessage.NOT_FOUND_USER.getMessage());

        assertThat(club.getMaster()).isEqualTo(previousMaster);
    }

    @Test
    @DisplayName("동아리를 관리할 권한이 없으면 위임이 이루어지지 않는다.")
    void transferClubMasterForbidden() {
        // given
        previousMaster = Fixture.createUserWithRole(UserRole.CLUB_MASTER);
        ReflectionTestUtils.setField(previousMaster, "id", PREVIOUS_MASTER_ID);
        prepareTransferContext(previousMaster);

        BDDMockito.given(clubRepository.findById(CLUB_ID)).willReturn(Optional.of(club));
        BDDMockito.willThrow(new MokkojiException(FailMessage.FORBIDDEN_MANAGE_CLUB))
                .given(manageAuthorizer).validateCanManageClub(any(), anyLong(), any());

        // when & then
        assertThatThrownBy(() -> clubMasterService.transferClubMaster(AuthRole.USER, PREVIOUS_MASTER_ID, CLUB_ID, NEXT_MASTER_USER_CODE))
                .isInstanceOf(MokkojiException.class)
                .hasMessage(FailMessage.FORBIDDEN_MANAGE_CLUB.getMessage());

        BDDMockito.verify(userRepository, BDDMockito.never()).findByCode(anyString());
        assertThat(club.getMaster()).isEqualTo(previousMaster);
    }
}
