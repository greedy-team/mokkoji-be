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
import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        if (userId == null) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }
        User adminUser = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        if (!adminUser.getRole().equals(UserRole.GREEDY_ADMIN)) {
            throw new MokkojiException(FailMessage.FORBIDDEN_REGISTER_CLUB);
        }

        String validStudentId = null;
        if (clubMasterStudentId != null && !clubMasterStudentId.isBlank()) {
            User masterUser = userRepository.findByStudentId(clubMasterStudentId)
                    .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
            masterUser.grantRole(UserRole.CLUB_MASTER);
            validStudentId = masterUser.getStudentId();
        }

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
        if (userId == null) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB));

        boolean isClubMaster = user.getRole().equals(UserRole.CLUB_MASTER);
        boolean isOwnerOfThisClub = user.getStudentId().equals(club.getClubMasterStudentId());

        if (!(isClubMaster && isOwnerOfThisClub)) {
            throw new MokkojiException(FailMessage.FORBIDDEN_MODIFY_CLUB);
        }

        return ClubManageDetailResponse.of(
                club.getName(),
                club.getClubCategory().name(),
                club.getClubAffiliation().name(),
                club.getDescription(),
                club.getLogo(),
                club.getInstagram()
        );
    }

    //TODO: 중복 로직 리팩토링 예정.
    @Transactional
    public ClubUpdateResponse updateClub(
            final Long userId, final Long clubId, final String name, final ClubCategory category, final ClubAffiliation affiliation, final String description, final String logo, final String instagram) {
        if (userId == null) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB));

        boolean isClubMaster = user.getRole().equals(UserRole.CLUB_MASTER);
        boolean isOwnerOfThisClub = user.getStudentId().equals(club.getClubMasterStudentId());

        if (!(isClubMaster && isOwnerOfThisClub)) {
            throw new MokkojiException(FailMessage.FORBIDDEN_MODIFY_CLUB);
        }

        String oldLogoKey = club.getLogo();
        String newLogoKey = (logo != null && !logo.isBlank()) ? logo : null;

        club.updateIfPresent(name, category, affiliation, description, logo, instagram);

        String updateUrl = (newLogoKey != null) ? appDataS3Client.getPresignedPutUrl(newLogoKey) : null;
        String deleteUrl = (newLogoKey != null && oldLogoKey != null && !oldLogoKey.equals(newLogoKey)) ? appDataS3Client.getPresignedDeleteUrl(oldLogoKey) : null;

        return ClubUpdateResponse.of(updateUrl, deleteUrl);
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
}
