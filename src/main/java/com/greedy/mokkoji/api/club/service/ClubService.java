package com.greedy.mokkoji.api.club.service;

import com.greedy.mokkoji.api.club.dto.response.*;
import com.greedy.mokkoji.api.club.dto.response.allClubs.*;
import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import com.greedy.mokkoji.enums.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final AppDataS3Client appDataS3Client;

    @Transactional(readOnly = true)
    public ClubDetailResponse findClub(final Long userId, final Long clubId) {

        final Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB));
        final Recruitment recruitment = recruitmentRepository
                .findTopByClubIdOrderByCreatedAtDesc(club.getId())
                .orElse(null);
        final Boolean isFavorite = getIsFavorite(userId, clubId);

        return mapToClubDetailResponse(club, recruitment, isFavorite);
    }

    @Transactional(readOnly = true)
    public ClubsPaginationResponse findClubsByConditions(final Long userId,
                                                         final String keyword,
                                                         final ClubCategory category,
                                                         final ClubAffiliation affiliation,
                                                         final RecruitStatus status,
                                                         final Pageable pageable) {

        final Page<Club> clubPage = clubRepository.searchClubs(keyword, category, affiliation, status, pageable);
        final List<Club> clubs = clubPage.getContent();
        final List<ClubResponse> clubResponses = mapToClubResponses(userId, clubs);

        final PageResponse pageResponse = createPageResponse(clubPage);

        return new ClubsPaginationResponse(clubResponses, pageResponse);
    }

    @Transactional(readOnly = true)
    public AllClubsResponse getAllClubs(
            final Long userId,
            final ClubAffiliation affiliation,
            final ClubCategory category,
            final Pageable pageable
    ) {
        Page<ClubWithLatestRecruitment> clubPage = clubRepository.findClubs(affiliation, category, pageable);
        List<ClubWithLatestRecruitment> filteredClubs = clubPage.getContent();
        List<ClubPreviewResponse> clubResponses = mapToClubPreviewResponses(userId, filteredClubs);

        PageResponse pageResponse = createPageResponse(clubPage);

        return AllClubsResponse.of(clubResponses, pageResponse);
    }

    @Transactional
    public void createClub(final Long userId, final String name, final ClubCategory category,
                           final ClubAffiliation affiliation, final String clubMasterStudentId) {
        validateClubRegistrar(userId);
        String validStudentId = getValidClubMasterStudentId(clubMasterStudentId);

        clubRepository.save(
                Club.builder()
                        .name(name)
                        .clubCategory(category)
                        .clubAffiliation(affiliation)
                        .clubMasterStudentId(validStudentId)
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public ClubManageDetailResponse getClubManageDetail(final Long userId, final Long clubId) {
        Club club = validateClubManagerAuthority(userId, clubId);

        return ClubManageDetailResponse.of(
                club.getName(),
                club.getClubCategory(),
                club.getClubAffiliation(),
                club.getDescription(),
                appDataS3Client.getPublicUrl(club.getLogo()),
                club.getInstagram()
        );
    }

    @Transactional
    public ClubUpdateResponse updateClub(
            final Long userId, final Long clubId, final String name, final ClubCategory category, final ClubAffiliation affiliation,
            final String description, final String clubMasterStudentId, final String logo, final String instagram
    ) {
        Club club = validateClubManagerAuthority(userId, clubId);

        String oldLogoKey = club.getLogo();
        String newLogoKey = extractLogoKey(clubId, logo);

        if (clubMasterStudentId != null) {
            changeClubMasterRole(club.getClubMasterStudentId(), clubMasterStudentId);
        }

        club.updateIfPresent(name, category, affiliation, description, clubMasterStudentId, newLogoKey, instagram);

        String updateLogo = generatePresignedPutUrl(newLogoKey);
        String deleteLogo = generatePresignedDeleteUrl(newLogoKey, oldLogoKey);

        return ClubUpdateResponse.of(updateLogo, deleteLogo);
    }

    private List<ClubPreviewResponse> mapToClubPreviewResponses(
            final Long userId,
            final List<ClubWithLatestRecruitment> clubs
    ) {
        return clubs.stream()
                .map(c -> mapToClubPreviewResponse(userId, c))
                .sorted(clubSortComparator(userId))
                .toList();
    }

    // 즐겨찾기 여부 → 모집 상태 → 마감일 순으로 정렬하는 Comparator 생성
    private Comparator<ClubPreviewResponse> clubSortComparator(Long userId) {
        Comparator<ClubPreviewResponse> recruitmentComparator =
                Comparator.comparing(
                        (ClubPreviewResponse r) -> r.recruitmentPreviewResponse(),
                        Comparator.nullsLast(
                                Comparator
                                        .comparing((RecruitmentPreviewResponse rp) -> rp.recruitStatus().getPriority())
                                        .thenComparing(RecruitmentPreviewResponse::recruitEnd)
                        )
                );

        Comparator<ClubPreviewResponse> base = recruitmentComparator;

        if (userId != null) {
            return Comparator.comparing(
                            (ClubPreviewResponse r) ->
                                    r.recruitmentPreviewResponse() != null &&
                                            r.recruitmentPreviewResponse().isFavorite()
                    )
                    .reversed()
                    .thenComparing(base);
        }

        return base;
    }

    private ClubPreviewResponse mapToClubPreviewResponse(Long userId, ClubWithLatestRecruitment c) {
        return ClubPreviewResponse.builder()
                .id(c.id())
                .name(c.name())
                .description(c.description())
                .logo(appDataS3Client.getPublicUrl(c.logo()))
                .recruitmentPreviewResponse(mapToLatestRecruitmentPreviewResponse(userId, c.id(), c.latestRecruitmentInfo()))
                .build();
    }

    @Nullable
    private RecruitmentPreviewResponse mapToLatestRecruitmentPreviewResponse(
            Long userId,
            Long clubId,
            LatestRecruitmentInfo latest
    ) {
        if (latest == null || latest.id() == null) {
            return null;
        }

        return RecruitmentPreviewResponse.builder()
                .id(latest.id())
                .recruitStart(latest.recruitStart())
                .recruitEnd(latest.recruitEnd())
                .recruitStatus(RecruitStatus.from(latest.isAlwaysRecruiting(), latest.recruitStart(), latest.recruitEnd()))
                .isFavorite(getIsFavorite(userId, clubId))
                .build();
    }


    private List<ClubResponse> mapToClubResponses(final Long userId, final List<Club> clubs) {
        return clubs.stream()
                .map(club -> {
                    Recruitment recruitment = recruitmentRepository.findTopByClubIdOrderByCreatedAtDesc(club.getId())
                            .orElse(null);
                    boolean isFavorite = getIsFavorite(userId, club.getId());
                    return ClubResponse.of(club.getId(),
                            club.getName(),
                            club.getClubCategory(),
                            club.getClubAffiliation(),
                            club.getDescription(),
                            recruitment != null ? recruitment.getRecruitStart() : null,
                            recruitment != null ? recruitment.getRecruitEnd() : null,
                            appDataS3Client.getPublicUrl(club.getLogo()),
                            isFavorite);
                })
                .sorted(getFavoriteComparator())
                .toList();
    }

    private ClubDetailResponse mapToClubDetailResponse(final Club club, final Recruitment recruitment,
                                                       final Boolean isFavorite) {
        return ClubDetailResponse.of(
                club.getId(),
                club.getName(),
                club.getClubCategory(),
                club.getClubAffiliation(),
                club.getDescription(),
                recruitment != null ? recruitment.getRecruitStart() : null,
                recruitment != null ? recruitment.getRecruitEnd() : null,
                appDataS3Client.getPublicUrl(club.getLogo()),
                isFavorite,
                club.getInstagram(),
                recruitment != null ? recruitment.getContent() : null
        );
    }

    private void validateClubRegistrar(final Long userId) { //권한 부여: GREEDY_ADMIN, CLUB_ADMIN
        User adminUser = findUserOrThrow(userId);
        if (!adminUser.getRole().canRegisterClub()) {
            throw new MokkojiException(FailMessage.FORBIDDEN_REGISTER_CLUB);
        }
    }

    private Club validateClubManagerAuthority(final Long userId, final Long clubId) { //권한 부여: CLUB_MASTER, CLUB_ADMIN
        User user = findUserOrThrow(userId);
        Club club = findClubOrThrow(clubId);

        if (!user.getRole().canManageClub(user, club)) {
            throw new MokkojiException(FailMessage.FORBIDDEN_MANAGE_CLUB);
        }

        return club;
    }

    private String getValidClubMasterStudentId(final String clubMasterStudentId) {
        if (clubMasterStudentId == null || clubMasterStudentId.isBlank()) {
            return null;
        }

        User masterUser = userRepository.findByStudentId(clubMasterStudentId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
        masterUser.updateRole(UserRole.CLUB_MASTER);
        return masterUser.getStudentId();
    }

    private void changeClubMasterRole(final String previousClubMasterStudentId, final String newClubMasterStudentId) {
        userRepository.findByStudentId(previousClubMasterStudentId)
                .ifPresent(user -> user.updateRole(UserRole.NORMAL));

        userRepository.findByStudentId(newClubMasterStudentId)
                .ifPresent(user -> user.updateRole(UserRole.CLUB_MASTER));
    }

    private Club findClubOrThrow(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB));
    }

    private User findUserOrThrow(Long userId) {
        if (userId == null) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
    }

    private boolean getIsFavorite(final Long userId, final Long clubId) {
        if (userId == null) { //회원 및 비회원 구별 로직
            return false;
        }
        return favoriteRepository.existsByUserIdAndClubId(userId, clubId);
    }

    private Comparator<ClubResponse> getFavoriteComparator() {
        return Comparator.comparing(ClubResponse::isFavorite).reversed();
    }

    private static PageResponse createPageResponse(final Page<?> page) {
        return PageResponse.of(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages(),
                (int) page.getTotalElements()
        );
    }

    @Nullable
    private String extractLogoKey(Long clubId, String logo) {
        if (logo == null || logo.isBlank()) {
            return null;
        }

        int dotIndex = logo.lastIndexOf('.');
        String prevDot = logo.substring(0, dotIndex);
        String nextDot = logo.substring(dotIndex); //jpg와 같은 확장자 부분
        String uuid = UUID.randomUUID().toString();
        String fileName = prevDot + "_" + uuid + nextDot;

        String logoKey = String.format("club-logo/%d/%s", clubId, fileName);
        return logoKey;
    }

    @Nullable
    private String generatePresignedPutUrl(final String newLogoKey) {
        return (newLogoKey != null)
                ? appDataS3Client.getPresignedPutUrl(newLogoKey)
                : null;
    }

    @Nullable
    private String generatePresignedDeleteUrl(String newLogoKey, String oldLogoKey) {
        return (newLogoKey != null && oldLogoKey != null && !oldLogoKey.equals(newLogoKey))
                ? appDataS3Client.getPresignedDeleteUrl(oldLogoKey)
                : null;
    }
}
