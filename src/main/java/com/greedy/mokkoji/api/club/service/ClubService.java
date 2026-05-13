package com.greedy.mokkoji.api.club.service;

import com.greedy.mokkoji.api.club.dto.response.ClubDetailResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubManageDetailResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubUpdateResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubsPaginationResponse;
import com.greedy.mokkoji.api.club.dto.response.allClubs.AllClubsResponse;
import com.greedy.mokkoji.api.club.dto.response.allClubs.ClubPreviewResponse;
import com.greedy.mokkoji.api.club.dto.response.allClubs.ClubWithLatestRecruitment;
import com.greedy.mokkoji.api.club.dto.response.allClubs.LatestRecruitmentInfo;
import com.greedy.mokkoji.api.club.dto.response.allClubs.RecruitmentPreviewResponse;
import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.university.repository.UniversityRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import com.greedy.mokkoji.enums.university.UniversityCode;
import com.greedy.mokkoji.enums.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final AppDataS3Client appDataS3Client;

    private static PageResponse createPageResponse(Pageable pageable, int totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());
        return PageResponse.of(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                totalPages,
                totalElements
        );
    }

    private static PageResponse createPageResponses(final Page<?> page) {
        return PageResponse.of(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages(),
                (int) page.getTotalElements()
        );
    }

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
    public ClubsPaginationResponse findClubsByConditions(
            final Long userId,
            final UniversityCode universityCode,
            final String keyword,
            final ClubCategory category,
            final ClubAffiliation affiliation,
            final RecruitStatus status,
            final Pageable pageable) {

        final Page<Club> clubPage = clubRepository.findClubsWithLatestRecruitment(universityCode, keyword, category, affiliation, status, pageable);
        final List<Club> clubs = clubPage.getContent();
        final List<ClubResponse> clubResponses = mapToClubResponses(userId, clubs);

        final PageResponse pageResponse = createPageResponses(clubPage);

        return new ClubsPaginationResponse(clubResponses, pageResponse);
    }

    @Transactional(readOnly = true)
    public AllClubsResponse getAllClubs(
            final Long userId,
            final UniversityCode universityCode,
            final String keyword,
            final ClubAffiliation affiliation,
            final ClubCategory category,
            final Pageable pageable
    ) {
        List<ClubWithLatestRecruitment> clubs = clubRepository.findAllClubsWithLatestRecruitment(universityCode, keyword, affiliation, category);

        Set<Long> favoriteClubIds = loadFavoriteClubIds(userId);

        List<ClubPreviewResponse> sortedResponses =
                toSortedClubPreviewResponses(clubs, favoriteClubIds, userId);

        List<ClubPreviewResponse> pageContent = slicePage(sortedResponses, pageable);

        PageResponse pageResponse = createPageResponse(pageable, sortedResponses.size());

        return AllClubsResponse.of(pageContent, pageResponse);
    }

    @Transactional
    public void createClub(final Long userId, final String name, final ClubCategory category,
                           final ClubAffiliation affiliation, final String clubMasterStudentId,
                           final UniversityCode universityCode) {
        validateClubRegistrar(userId);
        String validStudentId = getValidClubMasterStudentId(clubMasterStudentId);
        University university = universityRepository.findByCode(universityCode)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_UNIVERSITY));

        clubRepository.save(
                Club.builder()
                        .name(name)
                        .clubCategory(category)
                        .clubAffiliation(affiliation)
                        .clubMasterStudentId(validStudentId)
                        .university(university)
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

        return ClubUpdateResponse.of(clubId, updateLogo, deleteLogo);
    }

    private Set<Long> loadFavoriteClubIds(Long userId) {
        if (userId == null) return Set.of();
        return new HashSet<>(favoriteRepository.findClubIdsByUserId(userId));
    }

    private List<ClubPreviewResponse> toSortedClubPreviewResponses(
            List<ClubWithLatestRecruitment> clubs,
            Set<Long> favoriteClubIds,
            Long userId
    ) {
        return clubs.stream()
                .map(c -> mapToClubPreviewResponse(c, favoriteClubIds))
                .sorted(clubSortComparator(userId))
                .toList();
    }

    private ClubPreviewResponse mapToClubPreviewResponse(
            ClubWithLatestRecruitment c,
            Set<Long> favoriteClubIds
    ) {
        boolean isFavorite = favoriteClubIds.contains(c.id());

        return ClubPreviewResponse.builder()
                .id(c.id())
                .name(c.name())
                .description(c.description())
                .logo(appDataS3Client.getPublicUrl(c.logo()))
                .favorite(isFavorite)
                .recruitmentPreviewResponse(
                        mapToLatestRecruitmentPreviewResponse(c.latestRecruitmentInfo())
                )
                .build();
    }

    private List<ClubPreviewResponse> slicePage(
            List<ClubPreviewResponse> sortedResponses,
            Pageable pageable
    ) {
        int total = sortedResponses.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);

        if (start >= total) return List.of();
        return sortedResponses.subList(start, end);
    }

    // 즐겨찾기 여부 → 모집 상태 → 마감일 순으로 정렬하는 Comparator 생성
    private Comparator<ClubPreviewResponse> clubSortComparator(Long userId) {

        Comparator<RecruitmentPreviewResponse> recruitmentSortComparator =
                Comparator.nullsLast(
                        Comparator.comparing((RecruitmentPreviewResponse rp) -> rp.recruitStatus().getPriority())
                                .thenComparing(
                                        RecruitmentPreviewResponse::recruitEnd,
                                        Comparator.nullsLast(Comparator.naturalOrder())
                                )
                );

        Comparator<ClubPreviewResponse> recruitmentComparator =
                Comparator.comparing(ClubPreviewResponse::recruitmentPreviewResponse, recruitmentSortComparator);

        if (userId == null) return recruitmentComparator;

        return Comparator.comparing(ClubPreviewResponse::favorite)
                .reversed()
                .thenComparing(recruitmentComparator);
    }

    @Nullable
    private RecruitmentPreviewResponse mapToLatestRecruitmentPreviewResponse(
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
                .isAlwaysRecruiting(latest.isAlwaysRecruiting())
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
                            recruitment != null ? recruitment.isAlwaysRecruiting() : null,
                            calculateRecruitStatus(recruitment),
                            appDataS3Client.getPublicUrl(club.getLogo()),
                            isFavorite,
                            club.getUniversity().getName());
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

    private RecruitStatus calculateRecruitStatus(final Recruitment recruitment) {
        if (recruitment == null) return null;
        return RecruitStatus.from(
                recruitment.isAlwaysRecruiting(),
                recruitment.getRecruitStart(),
                recruitment.getRecruitEnd()
        );
    }
}
