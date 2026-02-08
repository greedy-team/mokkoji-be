package com.greedy.mokkoji.recruitment.service;

import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitment.AllRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitment.RecruitmentPreviewResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub.AllRecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub.RecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.createRecruitment.CreateRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.deleteRecruitment.DeleteRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.recentRecruitment.RecentRecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.specificRecruitment.SpecificRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.updateRecruitment.UpdateRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.service.RecruitmentService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.entity.RecruitmentImage;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentImageRepository;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.user.UserRole;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("모집글 서비스 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class RecruitmentServiceTest {

    @InjectMocks
    RecruitmentService recruitmentService;

    @Mock
    UserRepository userRepository;

    @Mock
    ClubRepository clubRepository;

    @Mock
    RecruitmentRepository recruitmentRepository;

    @Mock
    RecruitmentImageRepository recruitmentImageRepository;

    @Mock
    AppDataS3Client appDataS3Client;

    @Mock
    FavoriteRepository favoriteRepository;

    @Test
    @DisplayName("권한을 가진 관리자는 모집글을 생성할 수 있다.")
    void createRecruitment() {
        // given
        User adminUser = User.builder().build();
        ReflectionTestUtils.setField(adminUser, "id", 1L);
        ReflectionTestUtils.setField(adminUser, "role", UserRole.CLUB_MASTER);

        Club club = Club.builder()
                .name("그리디")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.ACADEMIC_CULTURAL)
                .logo("greedy-logo.png")
                .instagram("https://greedy-instagram.com")
                .description("그리디 소개글")
                .build();
        ReflectionTestUtils.setField(club, "id", 1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(adminUser));
        given(clubRepository.findById(1L)).willReturn(Optional.of(club));
        given(recruitmentRepository.save(any(Recruitment.class)))
                .willAnswer(inv -> {
                    Recruitment recruitment = inv.getArgument(0);
                    ReflectionTestUtils.setField(recruitment, "id", 1L);
                    return recruitment;
                });

        given(appDataS3Client.getPresignedPutUrl(any())).willReturn("putUrl");

        // when
        CreateRecruitmentResponse createRecruitmentResponse = recruitmentService.createRecruitment(
                1L,
                1L,
                "그리디 모집글 제목",
                "그리디 모집글 내용",
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 31, 23, 59),
                List.of("그리디 모집글 이미지1.jpg", "그리디 모집글 이미지2.png"),
                "그리디 모집 링크",
                false
        );

        // then
        assertThat(createRecruitmentResponse.id()).isEqualTo(1L);
        assertThat(createRecruitmentResponse.uploadImageUrls()).hasSize(2);
        assertThat(createRecruitmentResponse.uploadImageUrls()).allMatch(url -> url.equals("putUrl"));

        BDDMockito.verify(recruitmentRepository, times(1)).save(any(Recruitment.class));
        BDDMockito.verify(appDataS3Client, times(2)).getPresignedPutUrl(any());
        BDDMockito.verify(recruitmentImageRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("일반 사용자는 모집글을 생성할 수 없다.")
    void createRecruitment_forbidden() {
        // given
        User normalUser = User.builder().build();
        ReflectionTestUtils.setField(normalUser, "id", 1L);
        ReflectionTestUtils.setField(normalUser, "role", UserRole.NORMAL);

        given(userRepository.findById(1L)).willReturn(Optional.of(normalUser));

        // when, then
        Assertions.assertThatThrownBy(() -> recruitmentService.createRecruitment(
                        1L, 1L, "그리디 모집글 제목", "그리디 모집글 내용", LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                        List.of("그리디 모집글 이미지.jpgg"), "그리디 모집 링크", false
                ))
                .isInstanceOf(MokkojiException.class)
                .hasMessageContaining(FailMessage.FORBIDDEN.getMessage());
    }

    @Test
    @DisplayName("모집글 생성 시 동아리가 존재하지 않으면 예외가 발생한다.")
    void createRecruitment_notFoundClub() {
        // given
        User adminUser = User.builder().build();
        ReflectionTestUtils.setField(adminUser, "id", 1L);
        ReflectionTestUtils.setField(adminUser, "role", UserRole.CLUB_MASTER);

        given(userRepository.findById(1L)).willReturn(Optional.of(adminUser));
        given(clubRepository.findById(999L)).willReturn(Optional.empty());

        // when, then
        Assertions.assertThatThrownBy(() -> recruitmentService.createRecruitment(
                        1L, 999L, "그리디 모집글 제목", "그리디 모집글 내용", LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                        List.of("그리디 모집글 이미지.jpgg"), "그리디 모집 링크", false
                ))
                .isInstanceOf(MokkojiException.class)
                .hasMessageContaining(FailMessage.NOT_FOUND_CLUB.getMessage());
    }

    @Test
    @DisplayName("권한을 가진 관리자는 모집글을 수정할 수 있다.")
    void updateRecruitment() {
        // given
        User adminUser = User.builder().build();
        ReflectionTestUtils.setField(adminUser, "id", 1L);
        ReflectionTestUtils.setField(adminUser, "role", UserRole.CLUB_MASTER);

        Club club = Club.builder()
                .name("그리디")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.ACADEMIC_CULTURAL)
                .logo("greedy-logo.png")
                .instagram("https://greedy-instagram.com")
                .description("그리디 소개글")
                .build();
        ReflectionTestUtils.setField(club, "id", 1L);

        Recruitment recruitment = Recruitment.builder()
                .club(club)
                .title("그리디 모집글 제목")
                .content("그리디 모집글 내용")
                .recruitStart(LocalDateTime.of(2025, 1, 1, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 1, 31, 23, 59))
                .recruitForm("그리디 모집 링크")
                .isAlwaysRecruiting(false)
                .build();
        ReflectionTestUtils.setField(recruitment, "id", 1L);

        RecruitmentImage oldImage1 = RecruitmentImage.builder().recruitment(recruitment).image("오래된 그리디 모집글 이미지1.jpg")
                .build();
        RecruitmentImage oldImage2 = RecruitmentImage.builder().recruitment(recruitment).image("오래된 그리디 모집글 이미지2.jpg")
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(adminUser));
        given(recruitmentRepository.findById(1L)).willReturn(Optional.of(recruitment));
        given(recruitmentImageRepository.findByRecruitmentId(1L)).willReturn(List.of(oldImage1, oldImage2));
        given(appDataS3Client.getPresignedDeleteUrl(any())).willReturn("deleteUrl");
        given(appDataS3Client.getPresignedPutUrl(any())).willReturn("putUrl");

        // when
        UpdateRecruitmentResponse updateRecruitmentResponse = recruitmentService.updateRecruitment(
                1L,
                1L,
                "그리디 모집글 제목 수정",
                "그리디 모집글 내용 수정",
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 31, 23, 59),
                List.of("그리디 모집글 이미지 수정.jpg"),
                "그리디 모집 링크 수정",
                false
        );

        // then
        assertThat(updateRecruitmentResponse.id()).isEqualTo(1L);
        assertThat(updateRecruitmentResponse.deleteImageUrls()).hasSize(2);
        assertThat(updateRecruitmentResponse.deleteImageUrls()).allMatch(url -> url.equals("deleteUrl"));
        assertThat(updateRecruitmentResponse.uploadImageUrls()).hasSize(1);
        assertThat(updateRecruitmentResponse.uploadImageUrls()).allMatch(url -> url.equals("putUrl"));

        BDDMockito.verify(recruitmentImageRepository, times(1)).deleteAll(any());
        BDDMockito.verify(recruitmentImageRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("모집글 수정 시 모집글이 존재하지 않으면 예외가 발생한다.")
    void updateRecruitment_notFoundRecruitment() {
        // given
        User adminUser = User.builder().build();
        ReflectionTestUtils.setField(adminUser, "id", 1L);
        ReflectionTestUtils.setField(adminUser, "role", UserRole.CLUB_ADMIN);

        given(userRepository.findById(1L)).willReturn(Optional.of(adminUser));
        given(recruitmentRepository.findById(999L)).willReturn(Optional.empty());

        // when, then
        Assertions.assertThatThrownBy(() -> recruitmentService.updateRecruitment(
                        1L, 999L, "그리디 모집글 제목", "그리디 모집글 내용", LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                        List.of("그리디 모집글 이미지.jpgg"), "그리디 모집 링크", false
                ))
                .isInstanceOf(MokkojiException.class)
                .hasMessageContaining(FailMessage.NOT_FOUNT_RECRUITMENT.getMessage());
    }

    @Test
    @DisplayName("권한을 가진 관리자는 모집글을 삭제할 수 있다.")
    void deleteRecruitment() {
        // given
        User adminUser = User.builder().build();
        ReflectionTestUtils.setField(adminUser, "id", 1L);
        ReflectionTestUtils.setField(adminUser, "role", UserRole.CLUB_MASTER);

        Recruitment recruitment = Recruitment.builder()
                .title("그리디 모집글 제목")
                .content("그리디 모집글 내용")
                .build();
        ReflectionTestUtils.setField(recruitment, "id", 1L);

        RecruitmentImage recruitmentImage = RecruitmentImage.builder()
                .recruitment(recruitment)
                .image("그리디 모집글 이미지.jpg")
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(adminUser));
        given(recruitmentRepository.findById(1L)).willReturn(Optional.of(recruitment));
        given(recruitmentImageRepository.findByRecruitmentId(1L)).willReturn(List.of(recruitmentImage));
        given(appDataS3Client.getPresignedDeleteUrl(any())).willReturn("deleteUrl");

        // when
        DeleteRecruitmentResponse result = recruitmentService.deleteRecruitment(1L, 1L);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.deleteImageUrls()).hasSize(1);
        assertThat(result.deleteImageUrls()).contains("deleteUrl");

        BDDMockito.verify(recruitmentImageRepository, times(1)).deleteAll(any());
        BDDMockito.verify(recruitmentRepository, times(1)).delete(recruitment);
    }

    @Test
    @DisplayName("특정 동아리의 모든 모집글을 최신순으로 조회한다.")
    void getAllRecruitmentOfClub_sortedByNewest() {
        // given
        Club club = Club.builder()
                .name("그리디")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.ACADEMIC_CULTURAL)
                .logo("greedy-logo.png")
                .instagram("https://greedy-instagram.com")
                .description("그리디 소개글")
                .build();
        ReflectionTestUtils.setField(club, "id", 1L);

        Recruitment olderRecruitment = Recruitment.builder()
                .club(club)
                .title("오래된 그리디 모집글 제목")
                .content("오래된 그리디 모집글 내용")
                .recruitStart(LocalDateTime.of(2025, 1, 1, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 1, 31, 23, 59))
                .recruitForm("오래된 그리디 모집 링크")
                .isAlwaysRecruiting(false)
                .build();
        ReflectionTestUtils.setField(olderRecruitment, "id", 1L);
        ReflectionTestUtils.setField(olderRecruitment, "createdAt", LocalDateTime.of(2025, 1, 1, 0, 0));

        Recruitment newerRecruitment = Recruitment.builder()
                .club(club)
                .title("새 그리디 모집글 제목")
                .content("새 그리디 모집글 내용")
                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                .recruitEnd(LocalDateTime.of(2026, 1, 31, 23, 59))
                .recruitForm("새 그리디 모집 링크")
                .isAlwaysRecruiting(false)
                .build();
        ReflectionTestUtils.setField(newerRecruitment, "id", 2L);
        ReflectionTestUtils.setField(newerRecruitment, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));

        RecruitmentImage newerImage = RecruitmentImage.builder()
                .recruitment(newerRecruitment)
                .image("그리디 모집글 이미지.jpg")
                .build();

        given(recruitmentRepository.findAllByClubId(1L))
                .willReturn(List.of(olderRecruitment, newerRecruitment));

        // when
        AllRecruitmentOfClubResponse allRecruitmentOfClubResponse = recruitmentService.getAllRecruitmentOfClub(1L);

        // then
        assertThat(allRecruitmentOfClubResponse.recruitments()).hasSize(2);

        RecruitmentOfClubResponse firstRecruitment = allRecruitmentOfClubResponse.recruitments().get(0);
        assertThat(firstRecruitment.id()).isEqualTo(2L);
        assertThat(firstRecruitment.title()).isEqualTo("새 그리디 모집글 제목");

        RecruitmentOfClubResponse secondRecruitment = allRecruitmentOfClubResponse.recruitments().get(1);
        assertThat(secondRecruitment.id()).isEqualTo(1L);
        assertThat(secondRecruitment.title()).isEqualTo("오래된 그리디 모집글 제목");
    }

    @Test
    @DisplayName("동아리의 최신 모집글을 조회할 수 있다.")
    void getRecentRecruitmentOfClub() {
        // given
        Club club = Club.builder()
                .name("그리디")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.ACADEMIC_CULTURAL)
                .logo("greedy-logo.png")
                .instagram("https://greedy-instagram.com")
                .description("그리디 소개글")
                .build();
        ReflectionTestUtils.setField(club, "id", 1L);

        Recruitment olderRecruitment = Recruitment.builder()
                .club(club)
                .title("오래된 그리디 모집글 제목")
                .content("오래된 그리디 모집글 내용")
                .recruitStart(LocalDateTime.of(2025, 1, 1, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 1, 31, 23, 59))
                .recruitForm("오래된 그리디 모집 링크")
                .isAlwaysRecruiting(false)
                .build();
        ReflectionTestUtils.setField(olderRecruitment, "id", 1L);
        ReflectionTestUtils.setField(olderRecruitment, "createdAt", LocalDateTime.of(2025, 1, 1, 0, 0));

        Recruitment newerRecruitment = Recruitment.builder()
                .club(club)
                .title("새 그리디 모집글 제목")
                .content("새 그리디 모집글 내용")
                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                .recruitEnd(LocalDateTime.of(2026, 1, 31, 23, 59))
                .recruitForm("새 그리디 모집 링크")
                .isAlwaysRecruiting(false)
                .build();
        ReflectionTestUtils.setField(newerRecruitment, "id", 2L);
        ReflectionTestUtils.setField(newerRecruitment, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));

        RecruitmentImage olderImage = RecruitmentImage.builder()
                .recruitment(newerRecruitment)
                .image("오래된 그리디 모집글 이미지.jpg")
                .build();
        RecruitmentImage newerImage = RecruitmentImage.builder()
                .recruitment(newerRecruitment)
                .image("새 그리디 모집글 이미지.jpg")
                .build();

        given(recruitmentRepository.findTopByClubIdOrderByCreatedAtDesc(1L))
                .willReturn(Optional.of(newerRecruitment));
        given(recruitmentImageRepository.findByRecruitmentIdOrderByCreatedAtAsc(2L))
                .willReturn(List.of(newerImage));
        given(appDataS3Client.getPublicUrl(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(favoriteRepository.existsByUserIdAndClubId(1L, 1L)).willReturn(true);

        // when
        RecentRecruitmentOfClubResponse recentRecruitmentOfClubResponse = recruitmentService.getRecentRecruitmentOfClub(
                1L, 1L);

        // then
        assertThat(recentRecruitmentOfClubResponse.id()).isEqualTo(2L);
        assertThat(recentRecruitmentOfClubResponse.clubId()).isEqualTo(1L);
        assertThat(recentRecruitmentOfClubResponse.isFavorite()).isTrue();
        assertThat(recentRecruitmentOfClubResponse.imageUrls()).hasSize(1);
        assertThat(recentRecruitmentOfClubResponse.imageUrls()).contains("새 그리디 모집글 이미지.jpg");
    }

    @Test
    @DisplayName("동아리 최신 모집글이 없으면 모집글 관련 필드는 null 혹은 빈 값으로 반환한다.")
    void getRecentRecruitmentOfClub_whenNoRecruitment_shouldReturnEmptyResponse() {
        // given
        Club club = Club.builder()
                .name("그리디")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.ACADEMIC_CULTURAL)
                .logo("greedy-logo.png")
                .instagram("https://greedy-instagram.com")
                .description("그리디 소개글")
                .build();
        ReflectionTestUtils.setField(club, "id", 1L);


        given(clubRepository.findById(1L)).willReturn(Optional.of(club));
        given(recruitmentRepository.findTopByClubIdOrderByCreatedAtDesc(1L)).willReturn(Optional.empty());
        given(favoriteRepository.existsByUserIdAndClubId(1L, 1L)).willReturn(false);
        given(appDataS3Client.getPublicUrl(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        RecentRecruitmentOfClubResponse response =
                recruitmentService.getRecentRecruitmentOfClub(1L, 1L);

        // then
        assertThat(response.clubId()).isEqualTo(1L);
        assertThat(response.clubName()).isEqualTo("그리디");

        assertThat(response.id()).isNull();
        assertThat(response.title()).isNull();
        assertThat(response.content()).isNull();
        assertThat(response.recruitStart()).isNull();
        assertThat(response.recruitEnd()).isNull();
        assertThat(response.status()).isNull();
        assertThat(response.createdAt()).isNull();
        assertThat(response.recruitForm()).isNull();
        assertThat(response.imageUrls()).isNotNull();
        assertThat(response.imageUrls()).isEmpty();
        assertThat(response.isAlwaysRecruiting()).isFalse();
        assertThat(response.isFavorite()).isFalse();
    }

    @Test
    @DisplayName("특정 모집글을 상세 조회할 수 있다.")
    void getSpecificRecruitment() {
        // given
        Club club = Club.builder()
                .name("그리디")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.ACADEMIC_CULTURAL)
                .logo("greedy-logo.png")
                .instagram("https://greedy-instagram.com")
                .description("그리디 소개글")
                .build();
        ReflectionTestUtils.setField(club, "id", 1L);

        Recruitment recruitment = Recruitment.builder()
                .club(club)
                .title("그리디 모집글 제목")
                .content("그리디 모집글 내용")
                .recruitStart(LocalDateTime.of(2025, 1, 1, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 1, 31, 23, 59))
                .recruitForm("그리디 모집 링크")
                .isAlwaysRecruiting(false)
                .build();
        ReflectionTestUtils.setField(recruitment, "id", 1L);

        RecruitmentImage recruitmentImage = RecruitmentImage.builder().recruitment(recruitment).image("그리디 모집글 이미지.jpg")
                .build();

        given(recruitmentRepository.findRecruitmentById(1L)).willReturn(Optional.of(recruitment));
        given(recruitmentImageRepository.findByRecruitmentIdOrderByCreatedAtAsc(1L))
                .willReturn(List.of(recruitmentImage));
        given(appDataS3Client.getPublicUrl(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(favoriteRepository.existsByUserIdAndClubId(1L, 1L)).willReturn(true);

        // when
        SpecificRecruitmentResponse specificRecruitmentResponse = recruitmentService.getSpecificRecruitment(1L, 1L);

        // then
        assertThat(specificRecruitmentResponse.id()).isEqualTo(1L);
        assertThat(specificRecruitmentResponse.clubId()).isEqualTo(1L);
        assertThat(specificRecruitmentResponse.isFavorite()).isTrue();
        assertThat(specificRecruitmentResponse.imageUrls()).contains("그리디 모집글 이미지.jpg");
    }

    @Test
    @DisplayName("전체 모집글을 조회할 수 있다. - (즐겨 찾기, 마감 임박, 모집 중, 상시 모집, 모집 전, 모집 마감)대로 정렬된다.")
    void getAllRecruitment_sortedByPolicy() {
        // given
        Long userId = 1L;
        ClubAffiliation filteringByAffiliation = ClubAffiliation.DEPARTMENT_CLUB;
        ClubCategory filteringByCategory = ClubCategory.ACADEMIC_CULTURAL;

        LocalDateTime now = LocalDateTime.now();

        Club favoriteClub = Club.builder()
                .clubAffiliation(filteringByAffiliation)
                .clubCategory(filteringByCategory)
                .build();
        ReflectionTestUtils.setField(favoriteClub, "id", 1L);

        Club imminentClub = Club.builder()
                .clubAffiliation(filteringByAffiliation)
                .clubCategory(filteringByCategory).build();
        ReflectionTestUtils.setField(imminentClub, "id", 2L);

        Club openClub = Club.builder()
                .clubAffiliation(filteringByAffiliation)
                .clubCategory(filteringByCategory)
                .build();
        ReflectionTestUtils.setField(openClub, "id", 3L);

        Club alwaysClub = Club.builder()
                .clubAffiliation(filteringByAffiliation)
                .clubCategory(filteringByCategory)
                .build();
        ReflectionTestUtils.setField(alwaysClub, "id", 4L);

        Club beforeClub = Club.builder()
                .clubAffiliation(filteringByAffiliation)
                .clubCategory(filteringByCategory)
                .build();
        ReflectionTestUtils.setField(beforeClub, "id", 5L);

        Club closedClub = Club.builder()
                .clubAffiliation(filteringByAffiliation)
                .clubCategory(filteringByCategory)
                .build();
        ReflectionTestUtils.setField(closedClub, "id", 6L);

        //즐겨찾기
        Recruitment favoriteRecruitment = Recruitment.builder()
                .club(favoriteClub)
                .recruitStart(now.minusDays(2))
                .recruitEnd(now.plusDays(10))
                .isAlwaysRecruiting(false)
                .build();
        ReflectionTestUtils.setField(favoriteRecruitment, "id", 1L);

        //모집 마감 임박
        Recruitment imminentRecruitment = Recruitment.builder()
                .club(imminentClub)
                .recruitStart(now.minusDays(1))
                .recruitEnd(now.plusMinutes(30))
                .isAlwaysRecruiting(false)
                .build();
        ReflectionTestUtils.setField(imminentRecruitment, "id", 2L);

        //모집 중
        Recruitment openRecruitment = Recruitment.builder()
                .club(openClub)
                .recruitStart(now.minusDays(1))
                .recruitEnd(now.plusDays(30))
                .isAlwaysRecruiting(false)
                .build();
        ReflectionTestUtils.setField(openRecruitment, "id", 3L);

        //상시 모집
        Recruitment alwaysRecruitment = Recruitment.builder()
                .club(alwaysClub)
                .recruitStart(now.minusDays(100))
                .recruitEnd(now.plusDays(1000))
                .isAlwaysRecruiting(true)
                .build();
        ReflectionTestUtils.setField(alwaysRecruitment, "id", 4L);

        //모집 전
        Recruitment beforeRecruitment = Recruitment.builder()
                .club(beforeClub)
                .recruitStart(now.plusDays(3))
                .recruitEnd(now.plusDays(10))
                .isAlwaysRecruiting(false)
                .build();
        ReflectionTestUtils.setField(beforeRecruitment, "id", 5L);

        //모집 마감
        Recruitment closedRecruitment = Recruitment.builder()
                .club(closedClub)
                .recruitStart(now.minusDays(10))
                .recruitEnd(now.minusDays(1))
                .isAlwaysRecruiting(false)
                .build();
        ReflectionTestUtils.setField(closedRecruitment, "id", 6L);

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Recruitment> page = new PageImpl<>(
                List.of(openRecruitment, closedRecruitment, alwaysRecruitment, imminentRecruitment, beforeRecruitment, favoriteRecruitment),
                pageable,
                6
        );

        given(recruitmentRepository.findRecruitments(
                        filteringByAffiliation,
                        filteringByCategory,
                        pageable
                ))
                .willReturn(page);

        given(favoriteRepository.existsByUserIdAndClubId(userId, 1L)).willReturn(true);
        given(favoriteRepository.existsByUserIdAndClubId(userId, 2L)).willReturn(false);
        given(favoriteRepository.existsByUserIdAndClubId(userId, 3L)).willReturn(false);
        given(favoriteRepository.existsByUserIdAndClubId(userId, 4L)).willReturn(false);
        given(favoriteRepository.existsByUserIdAndClubId(userId, 5L)).willReturn(false);
        given(favoriteRepository.existsByUserIdAndClubId(userId, 6L)).willReturn(false);

        given(appDataS3Client.getPublicUrl(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        AllRecruitmentResponse allRecruitmentResponse = recruitmentService.getAllRecruitment(
                userId,
                filteringByAffiliation,
                filteringByCategory,
                pageable
        );

        // then
        assertThat(allRecruitmentResponse.recruitments()).hasSize(6);

        List<Long> recruitmentIds = allRecruitmentResponse.recruitments().stream()
                .map(RecruitmentPreviewResponse::id)
                .toList();

        assertThat(recruitmentIds).containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
    }
}
