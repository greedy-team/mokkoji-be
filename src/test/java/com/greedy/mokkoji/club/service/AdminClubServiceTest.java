package com.greedy.mokkoji.club.service;

import com.greedy.mokkoji.api.auth.service.ManageAuthorizer;
import com.greedy.mokkoji.api.club.service.AdminClubService;
import com.greedy.mokkoji.api.external.AfterCommitS3Cleaner;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.admin.repository.AdminRepository;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.clubmaster.repository.ClubMasterApplicationRepository;
import com.greedy.mokkoji.db.comment.repository.CommentRepository;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentApplyClickRepository;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentImageRepository;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.enums.auth.AuthRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("관리자 동아리 삭제 서비스 테스트")
@ExtendWith(MockitoExtension.class)
class AdminClubServiceTest {

    @InjectMocks
    private AdminClubService adminClubService;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private RecruitmentRepository recruitmentRepository;

    @Mock
    private RecruitmentImageRepository recruitmentImageRepository;

    @Mock
    private RecruitmentApplyClickRepository recruitmentApplyClickRepository;

    @Mock
    private ClubMasterApplicationRepository clubMasterApplicationRepository;

    @Mock
    private AfterCommitS3Cleaner afterCommitS3Cleaner;

    @Mock
    private ManageAuthorizer manageAuthorizer;

    private static final Long ADMIN_ID = 1L;
    private static final Long CLUB_ID = 10L;

    private Club givenClubWithRecruitments(final List<Long> recruitmentIds) {
        final University university = Fixture.createUniversity();
        final Club club = Fixture.createClub(university);
        ReflectionTestUtils.setField(club, "id", CLUB_ID);

        given(clubRepository.findById(CLUB_ID)).willReturn(Optional.of(club));
        given(recruitmentRepository.findIdsByClubId(CLUB_ID)).willReturn(recruitmentIds);

        return club;
    }

    @Test
    @DisplayName("동아리를 삭제하면 모집글의 지원하기 클릭도 함께 삭제된다.")
    void deleteClubDeletesApplyClicks() {
        // given
        final List<Long> recruitmentIds = List.of(100L, 101L);
        givenClubWithRecruitments(recruitmentIds);

        // when
        adminClubService.deleteClub(AuthRole.ADMIN, ADMIN_ID, CLUB_ID);

        // then
        verify(recruitmentApplyClickRepository, times(1)).deleteByRecruitmentIdIn(recruitmentIds);
    }

    @Test
    @DisplayName("지원하기 클릭은 모집글보다 먼저 삭제된다. (외래키 제약 위반 방지)")
    void deleteClubDeletesApplyClicksBeforeRecruitments() {
        // given
        final List<Long> recruitmentIds = List.of(100L);
        givenClubWithRecruitments(recruitmentIds);

        // when
        adminClubService.deleteClub(AuthRole.ADMIN, ADMIN_ID, CLUB_ID);

        // then
        final InOrder inOrder = inOrder(recruitmentApplyClickRepository, recruitmentRepository);
        inOrder.verify(recruitmentApplyClickRepository).deleteByRecruitmentIdIn(recruitmentIds);
        inOrder.verify(recruitmentRepository).deleteByClubId(CLUB_ID);
    }

    @Test
    @DisplayName("동아리 삭제 시 모든 연관 데이터를 삭제한 뒤 동아리를 삭제한다.")
    void deleteClubDeletesAllAssociationsBeforeClub() {
        // given
        final List<Long> recruitmentIds = List.of(100L);
        final Club club = givenClubWithRecruitments(recruitmentIds);

        // when
        adminClubService.deleteClub(AuthRole.ADMIN, ADMIN_ID, CLUB_ID);

        // then
        verify(recruitmentApplyClickRepository, times(1)).deleteByRecruitmentIdIn(recruitmentIds);
        verify(recruitmentImageRepository, times(1)).deleteByRecruitmentIdIn(recruitmentIds);
        verify(recruitmentRepository, times(1)).deleteByClubId(CLUB_ID);
        verify(commentRepository, times(1)).deleteByClubId(CLUB_ID);
        verify(favoriteRepository, times(1)).deleteByClubId(CLUB_ID);
        verify(clubMasterApplicationRepository, times(1)).deleteByClubId(CLUB_ID);

        final InOrder inOrder = inOrder(clubMasterApplicationRepository, clubRepository);
        inOrder.verify(clubMasterApplicationRepository).deleteByClubId(CLUB_ID);
        inOrder.verify(clubRepository).delete(club);
    }
}
