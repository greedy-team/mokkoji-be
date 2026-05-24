package com.greedy.mokkoji.club.service;

import com.greedy.mokkoji.api.club.dto.response.ClubDetailResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubUpdateResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubsPaginationResponse;
import com.greedy.mokkoji.api.club.dto.response.allClubs.AllClubsResponse;
import com.greedy.mokkoji.api.club.dto.response.allClubs.ClubWithLatestRecruitment;
import com.greedy.mokkoji.api.club.dto.response.allClubs.LatestRecruitmentInfo;
import com.greedy.mokkoji.api.auth.service.ClubManageAuthorizer;
import com.greedy.mokkoji.api.club.service.ClubService;
import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.university.UniversityCode;
import com.greedy.mokkoji.enums.message.FailMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("동아리 검색 및 필터링 서비스 테스트")
@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @InjectMocks
    private ClubService clubService;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private RecruitmentRepository recruitmentRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ClubManageAuthorizer clubManageAuthorizer;

    @Mock
    private AppDataS3Client appDataS3Client;

    private Club club1;
    private Club club2;
    private Recruitment recruitment1;
    private Recruitment recruitment2;

    @BeforeEach
    void setUp() {
        final University university = University.builder()
                .name("세종대학교")
                .code(UniversityCode.SEJONG)
                .build();

        club1 = Club.builder()
                .name("testClub1")
                .university(university)
                .clubCategory(ClubCategory.ACADEMIC_CULTURAL)
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .description("testDescription1")
                .logo("testLogo1")
                .instagram("testInstagramURL1")
                .build();
        ReflectionTestUtils.setField(club1, "id", 1L);

        recruitment1 = Recruitment.builder()
                .club(club1)
                .content("testContent1")
                .recruitStart(LocalDateTime.of(2025, 1, 1, 12, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 30, 12, 0))
                .build();

        club2 = Club.builder()
                .name("testClub2")
                .university(university)
                .clubCategory(ClubCategory.CULTURAL_ART)
                .clubAffiliation(ClubAffiliation.DEPARTMENT_CLUB)
                .description("testDescription2")
                .logo("testLogo2")
                .instagram("testInstagramURL2")
                .build();
        ReflectionTestUtils.setField(club2, "id", 2L);

        recruitment2 = Recruitment.builder()
                .club(club2)
                .content("testContent2")
                .recruitStart(LocalDateTime.of(2025, 1, 1, 12, 0))
                .recruitEnd(LocalDateTime.of(2025, 1, 30, 12, 0))
                .build();
    }

    @Test
    @DisplayName("전체 동아리 정보를 조회한다.")
    void findAllClubs() {
        //given
        final Long userId = 1L;
        final Long clubId1 = club1.getId();
        final Long clubId2 = club2.getId();
        final Pageable pageable = PageRequest.of(0, 10);

        final LatestRecruitmentInfo latestRecruitmentInfo1 = new LatestRecruitmentInfo(
                1L,
                LocalDateTime.of(2025, 1, 1, 12, 0),
                LocalDateTime.of(2025, 3, 30, 12, 0),
                false
        );

        final LatestRecruitmentInfo latestRecruitmentInfo2 = new LatestRecruitmentInfo(
                2L,
                LocalDateTime.of(2025, 1, 1, 12, 0),
                LocalDateTime.of(2025, 1, 30, 12, 0),
                false
        );

        final ClubWithLatestRecruitment clubWithRecruitment1 = new ClubWithLatestRecruitment(
                clubId1,
                "testClub1",
                "세종대학교",
                "testDescription1",
                "testLogo1",
                latestRecruitmentInfo1
        );

        final ClubWithLatestRecruitment clubWithRecruitment2 = new ClubWithLatestRecruitment(
                clubId2,
                "testClub2",
                "세종대학교",
                "testDescription2",
                "testLogo2",
                latestRecruitmentInfo2
        );

        BDDMockito.given(clubRepository.findAllClubsWithLatestRecruitment(any(), any(), any(), any())).willReturn(List.of(clubWithRecruitment1, clubWithRecruitment2));
        BDDMockito.given(favoriteRepository.findClubIdsByUserId(userId)).willReturn(List.of(clubId1));
        BDDMockito.given(appDataS3Client.getPublicUrl("testLogo1")).willReturn("testLogo1");
        BDDMockito.given(appDataS3Client.getPublicUrl("testLogo2")).willReturn("testLogo2");

        //when
        AllClubsResponse response = clubService.getAllClubs(userId, null, null, null, null, pageable);

        //then
        assertThat(response.clubs()).hasSize(2);

        assertThat(response.clubs().get(0).name()).isEqualTo("testClub1");
        assertThat(response.clubs().get(0).description()).isEqualTo("testDescription1");
        assertThat(response.clubs().get(0).logo()).isEqualTo("testLogo1");
        assertThat(response.clubs().get(0).favorite()).isTrue();

        assertThat(response.clubs().get(1).name()).isEqualTo("testClub2");
        assertThat(response.clubs().get(1).description()).isEqualTo("testDescription2");
        assertThat(response.clubs().get(1).logo()).isEqualTo("testLogo2");
        assertThat(response.clubs().get(1).favorite()).isFalse();

        assertThat(response.page().totalElements()).isEqualTo(2);

        verify(clubRepository, times(1)).findAllClubsWithLatestRecruitment(any(), any(), any(), any());
        verify(favoriteRepository, times(1)).findClubIdsByUserId(userId);
    }

    @Test
    @DisplayName("동아리 상세 정보를 조회한다.")
    void findClubDetailInformation() {
        //given
        final Long userId = 1L;
        final Long clubId = club1.getId();

        BDDMockito.given(clubRepository.findById(clubId)).willReturn(Optional.ofNullable(club1));
        BDDMockito.given(recruitmentRepository.findTopByClubIdOrderByCreatedAtDesc(clubId)).willReturn(Optional.ofNullable(recruitment1));
        BDDMockito.given(favoriteRepository.existsByUserIdAndClubId(userId, clubId)).willReturn(true);
        BDDMockito.given(appDataS3Client.getPublicUrl(club1.getLogo())).willReturn("testLogo1");

        //when
        ClubDetailResponse response = clubService.findClub(userId, clubId);

        //then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("testClub1");
        assertThat(response.category()).isEqualTo(ClubCategory.ACADEMIC_CULTURAL);
        assertThat(response.affiliation()).isEqualTo(ClubAffiliation.CENTRAL_CLUB);
        assertThat(response.description()).isEqualTo("testDescription1");
        assertThat(response.recruitStartDate()).isEqualTo("2025-01-01");
        assertThat(response.recruitEndDate()).isEqualTo("2025-03-30");
        assertThat(response.logo()).isEqualTo("testLogo1");
        assertThat(response.isFavorite()).isEqualTo(true);
        assertThat(response.instagram()).isEqualTo("testInstagramURL1");
        assertThat(response.recruitPost()).isEqualTo("testContent1");

        verify(clubRepository, times(1)).findById(anyLong());
        verify(recruitmentRepository, times(1)).findTopByClubIdOrderByCreatedAtDesc(anyLong());
        verify(favoriteRepository, times(1)).existsByUserIdAndClubId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 클럽을 조회하면 예외를 던진다")
    void getClubShouldThrowExceptionWhenNotExists() {
        //given
        final Long userId = 1L;
        final Long nonExistentClubId = 99999L;

        BDDMockito.given(clubRepository.findById(nonExistentClubId)).willReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> clubService.findClub(userId, nonExistentClubId))
                .isInstanceOf(MokkojiException.class)
                .hasMessage(FailMessage.NOT_FOUND_CLUB.getMessage());
    }

    @Test
    @DisplayName("모집이 존재하지 않는 경우 null 값을 가진 응답을 반환한다")
    void findClubDetailWhenRecruitmentNotExists() {
        //given
        final Long userId = 1L;
        final Club clubWithoutRecruitment = Club.builder()
                .name("testClub3")
                .clubCategory(ClubCategory.SPORTS)
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .description("testDescription3")
                .logo("testLogo3")
                .instagram("testInstagramURL3")
                .build();
        ReflectionTestUtils.setField(clubWithoutRecruitment, "id", 3L);
        final Long clubId = clubWithoutRecruitment.getId();

        BDDMockito.given(clubRepository.findById(clubId)).willReturn(Optional.of(clubWithoutRecruitment));
        BDDMockito.given(recruitmentRepository.findTopByClubIdOrderByCreatedAtDesc(clubId)).willReturn(Optional.empty());
        BDDMockito.given(favoriteRepository.existsByUserIdAndClubId(userId, clubId)).willReturn(false);
        BDDMockito.given(appDataS3Client.getPublicUrl(clubWithoutRecruitment.getLogo())).willReturn("testLogo3");

        //when
        ClubDetailResponse response = clubService.findClub(userId, clubId);

        //then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("testClub3");
        assertThat(response.category()).isEqualTo(ClubCategory.SPORTS);
        assertThat(response.recruitStartDate()).isNull();
        assertThat(response.recruitEndDate()).isNull();
        assertThat(response.recruitPost()).isNull();

        verify(clubRepository, times(1)).findById(clubId);
        verify(recruitmentRepository, times(1)).findTopByClubIdOrderByCreatedAtDesc(clubId);
        verify(favoriteRepository, times(1)).existsByUserIdAndClubId(userId, clubId);
    }

    @Test
    @DisplayName("권한이 있으면 동아리 정보를 수정한다")
    void updateClub() {
        //given
        final Long userId = 1L;
        final Long clubId = club1.getId();
        final String updatedName = "수정된 동아리명";
        final ClubCategory updatedCategory = ClubCategory.SPORTS;
        final ClubAffiliation updatedAffiliation = ClubAffiliation.DEPARTMENT_CLUB;
        final String updatedDescription = "수정된 설명";
        final String updatedLogo = "updatedLogo.jpg";
        final String updatedInstagram = "updatedInstagram";

        BDDMockito.given(clubRepository.findById(clubId)).willReturn(Optional.of(club1));
        BDDMockito.given(appDataS3Client.getPresignedPutUrl(any())).willReturn("presignedPutUrl");

        //when
        ClubUpdateResponse response = clubService.updateClub(AuthRole.USER, userId, clubId, updatedName, updatedCategory,
                updatedAffiliation, updatedDescription, updatedLogo, updatedInstagram);

        //then
        assertThat(response).isNotNull();

        verify(clubManageAuthorizer, times(1)).validateCanManageClub(AuthRole.USER, userId, club1);
        verify(clubRepository, times(1)).findById(clubId);
    }

    @Test
    @DisplayName("조건 검색 시 즐겨찾기한 동아리가 목록 상단에 표시된다")
    void findClubsByConditionsSortedByFavorite() {
        //given
        final Long userId = 1L;
        final Long clubId1 = club1.getId();
        final Long clubId2 = club2.getId();
        final Pageable pageable = PageRequest.of(0, 10);
        final List<Club> clubs = List.of(club1, club2);
        final Page<Club> clubPage = new PageImpl<>(clubs, pageable, clubs.size());

        BDDMockito.given(clubRepository.findClubsWithLatestRecruitment(any(), any(), any(), any(), any(), any())).willReturn(clubPage);
        BDDMockito.given(recruitmentRepository.findTopByClubIdOrderByCreatedAtDesc(clubId1)).willReturn(Optional.ofNullable(recruitment1));
        BDDMockito.given(recruitmentRepository.findTopByClubIdOrderByCreatedAtDesc(clubId2)).willReturn(Optional.ofNullable(recruitment2));
        BDDMockito.given(favoriteRepository.existsByUserIdAndClubId(userId, clubId1)).willReturn(false);
        BDDMockito.given(favoriteRepository.existsByUserIdAndClubId(userId, clubId2)).willReturn(true);
        BDDMockito.given(appDataS3Client.getPublicUrl(club1.getLogo())).willReturn("testLogo1");
        BDDMockito.given(appDataS3Client.getPublicUrl(club2.getLogo())).willReturn("testLogo2");

        //when
        final ClubsPaginationResponse response = clubService.findClubsByConditions(userId, null, null, null, null, null, pageable);

        //then
        assertThat(response.clubs()).hasSize(2);
        assertThat(response.clubs().get(0).name()).isEqualTo("testClub2");
        assertThat(response.clubs().get(0).isFavorite()).isTrue();
        assertThat(response.clubs().get(1).name()).isEqualTo("testClub1");
        assertThat(response.clubs().get(1).isFavorite()).isFalse();
    }
}
