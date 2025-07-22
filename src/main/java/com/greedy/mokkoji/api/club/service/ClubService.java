package com.greedy.mokkoji.api.club.service;

import com.greedy.mokkoji.api.club.dto.club.response.*;
import com.greedy.mokkoji.api.club.dto.page.PageResponse;
import com.greedy.mokkoji.api.external.AppDataS3Client;
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

import java.util.List;
import java.util.stream.Collectors;

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
        final Recruitment recruitment = recruitmentRepository.findByClubId(club.getId());
        final Boolean isFavorite = getIsFavorite(userId, clubId);

        return mapToClubDetailResponse(club, recruitment, isFavorite);
    }

    @Transactional(readOnly = true)
    public ClubSearchResponse findClubsByConditions(final Long userId,
                                                    final String keyword,
                                                    final ClubCategory category,
                                                    final ClubAffiliation affiliation,
                                                    final RecruitStatus status,
                                                    final Pageable pageable) {

        final Page<Club> clubPage = clubRepository.findClubs(keyword, category, affiliation, status, pageable);

        final List<ClubResponse> clubResponses = mapToClubResponses(userId, clubPage.getContent());
        final PageResponse pageResponse = createPageResponse(clubPage);

        return new ClubSearchResponse(clubResponses, pageResponse);
    }

    @Transactional
    public void createClub(final Long userId, final String name, final ClubCategory category, final ClubAffiliation affiliation, final String clubMasterStudentId) {
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
                club.getClubCategory().name(),
                club.getClubAffiliation().name(),
                club.getDescription(),
                club.getLogo(),
                club.getInstagram()
        );
    }

    @Transactional
    public ClubUpdateResponse updateClub(
            final Long userId, final Long clubId, final String name, final ClubCategory category, final ClubAffiliation affiliation, final String description, final String logo, final String instagram) {
        Club club = validateClubManagerAuthority(userId, clubId);

        String oldLogoKey = club.getLogo();
        String newLogoKey = extractNewLogoKey(logo);

        club.updateIfPresent(name, category, affiliation, description, logo, instagram);

        String updateLogo = generatePresignedPutUrl(newLogoKey);
        String deleteLogo = generatePresignedDeleteUrl(newLogoKey, oldLogoKey);

        return ClubUpdateResponse.of(updateLogo, deleteLogo);
    }

    private boolean getIsFavorite(final Long userId, final Long clubId) {
        if (userId == null) { //회원 및 비회원 구별 로직
            return false;
        }
        return favoriteRepository.existsByUserIdAndClubId(userId, clubId);
    }

    private ClubDetailResponse mapToClubDetailResponse(final Club club, final Recruitment recruitment, final Boolean isFavorite) {
        return ClubDetailResponse.of(
                club.getId(),
                club.getName(),
                club.getClubCategory().getDescription(),
                club.getClubAffiliation().getDescription(),
                club.getDescription(),
                recruitment.getRecruitStart(),
                recruitment.getRecruitEnd(),
                appDataS3Client.getPresignedUrl(club.getLogo()),
                isFavorite,
                club.getInstagram(),
                recruitment.getContent()
        );
    }

    private List<ClubResponse> mapToClubResponses(final Long userId, final List<Club> clubs) {
        return clubs.stream()
                .map(club -> {
                    Recruitment recruitment = recruitmentRepository.findByClubId(club.getId());
                    boolean isFavorite = getIsFavorite(userId, club.getId());
                    return ClubResponse.of(club.getId(),
                            club.getName(),
                            club.getClubCategory().getDescription(),
                            club.getClubAffiliation().getDescription(),
                            club.getDescription(),
                            recruitment.getRecruitStart(),
                            recruitment.getRecruitEnd(),
                            appDataS3Client.getPresignedUrl(club.getLogo()),
                            isFavorite);
                })
                .collect(Collectors.toList());
    }

    private PageResponse createPageResponse(final Page<Club> clubPage) {
        return PageResponse.of(
                clubPage.getNumber() + 1,
                clubPage.getSize(),
                clubPage.getTotalPages(),
                (int) clubPage.getTotalElements()
        );
    }

    private void validateClubRegistrar(final Long userId) { //권한 부여: GREEDY_ADMIN, CLUB_ADMIN
        User adminUser = findUserOrThrow(userId);
        if (!adminUser.getRole().canRegisterClub()) {
            throw new MokkojiException(FailMessage.FORBIDDEN_REGISTER_CLUB);
        }
    }

    private String getValidClubMasterStudentId(final String clubMasterStudentId) {
        if (clubMasterStudentId == null || clubMasterStudentId.isBlank()) {
            return null;
        }

        User masterUser = userRepository.findByStudentId(clubMasterStudentId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
        masterUser.grantRole(UserRole.CLUB_MASTER);
        return masterUser.getStudentId();
    }

    private Club validateClubManagerAuthority(final Long userId, final Long clubId) { //권한 부여: CLUB_MASTER, CLUB_ADMIN
        User user = findUserOrThrow(userId);
        Club club = findClubOrThrow(clubId);

        if (!user.getRole().canManageClub(user, club)) {
            throw new MokkojiException(FailMessage.FORBIDDEN_MANAGE_CLUB);
        }

        return club;
    }

    private User findUserOrThrow(Long userId) {
        if (userId == null) throw new MokkojiException(FailMessage.UNAUTHORIZED);
        return userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
    }

    private Club findClubOrThrow(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB));
    }

    @Nullable
    private String extractNewLogoKey(String logo) {
        return (logo != null && !logo.isBlank())
                ? logo
                : null;
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
