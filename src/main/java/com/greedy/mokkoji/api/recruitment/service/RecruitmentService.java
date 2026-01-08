package com.greedy.mokkoji.api.recruitment.service;

import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitment.AllRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitment.ClubPreviewResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitment.RecruitmentPreviewResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub.AllRecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub.RecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.createRecruitment.CreateRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.deleteRecruitment.DeleteRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.recentRecruitment.RecentRecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.specificRecruitment.SpecificRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.updateRecruitment.UpdateRecruitmentResponse;
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
import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import com.greedy.mokkoji.enums.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentImageRepository recruitmentImageRepository;
    private final AppDataS3Client appDataS3Client;
    private final FavoriteRepository favoriteRepository;

    @Transactional
    public CreateRecruitmentResponse createRecruitment(
            final Long userId,
            final Long clubId,
            final String title,
            final String content,
            final LocalDateTime recruitStart,
            final LocalDateTime recruitEnd,
            final List<String> imageNames,
            final String recruitForm,
            final boolean isAlwaysRecruiting) {

        validateAdmin(userId);

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB));

        Recruitment recruitment = buildAndSaveRecruitment(club, title, content, recruitStart, recruitEnd, recruitForm, isAlwaysRecruiting);
        List<String> uploadImageUrls = uploadRecruitmentImages(recruitment, imageNames);

        return CreateRecruitmentResponse.of(recruitment.getId(), uploadImageUrls);
    }

    @Transactional
    public UpdateRecruitmentResponse updateRecruitment(
            final Long userId,
            final Long recruitmentId,
            final String title,
            final String content,
            final LocalDateTime recruitStart,
            final LocalDateTime recruitEnd,
            final List<String> imageNames,
            final String recruitForm,
            final boolean isAlwaysRecruiting
    ) {
        validateAdmin(userId);

        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUNT_RECRUITMENT));

        recruitment.updateRecruitment(title, content, recruitStart, recruitEnd, recruitForm, isAlwaysRecruiting);

        List<String> deleteImageUrls = deleteImages(recruitmentId);
        List<String> uploadImageUrls = uploadRecruitmentImages(recruitment, imageNames);

        return UpdateRecruitmentResponse.of(recruitment.getId(), deleteImageUrls, uploadImageUrls);
    }

    @Transactional
    public DeleteRecruitmentResponse deleteRecruitment(final Long userId, final Long recruitmentId) {
        validateAdmin(userId);

        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUNT_RECRUITMENT));

        List<String> deleteImageUrls = deleteImages(recruitmentId);
        recruitmentRepository.delete(recruitment);

        return DeleteRecruitmentResponse.of(recruitmentId, deleteImageUrls);
    }

    @Transactional
    public AllRecruitmentOfClubResponse getAllRecruitmentOfClub(final Long clubId) {
        List<Recruitment> recruitments = recruitmentRepository.findAllByClubId(clubId);

        List<RecruitmentOfClubResponse> recruitmentList = recruitments.stream()
                .sorted(Comparator.comparing(Recruitment::getRecruitEnd).reversed())
                .map(recruitment -> RecruitmentOfClubResponse.builder()
                        .id(recruitment.getId())
                        .title(recruitment.getTitle())
                        .content(recruitment.getContent())
                        .recruitStart(recruitment.getRecruitStart())
                        .recruitEnd(recruitment.getRecruitEnd())
                        .status(RecruitStatus.from(recruitment.getRecruitStart(), recruitment.getRecruitEnd()))
                        .createdAt(recruitment.getCreatedAt())
                        .firstImage(getFirstImageUrl(recruitment.getId()))
                        .isAlwaysRecruiting(recruitment.isAlwaysRecruiting())
                        .build()
                )
                .toList();

        return AllRecruitmentOfClubResponse.of(recruitmentList);
    }

    @Transactional
    public RecentRecruitmentOfClubResponse getRecentRecruitmentOfClub(final Long clubId, final Long userId) {

        Optional<Recruitment> recentRecruitment = recruitmentRepository.findTopByClubIdOrderByUpdatedAtDesc(clubId);

        Recruitment recruitment = recentRecruitment.orElseGet(() ->
                recruitmentRepository.findTopByClubIdAndIsAlwaysRecruitingOrderByUpdatedAtDesc(clubId, true)
                        .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUNT_RECRUITMENT))
        );

        Club club = recruitment.getClub();

        List<RecruitmentImage> recruitmentImages = recruitmentImageRepository.findByRecruitmentIdOrderByCreatedAtAsc(
                recruitment.getId());

        List<String> imageUrls = recruitmentImages.stream()
                .map(image -> appDataS3Client.getPublicUrl(image.getImage()))
                .toList();

        Boolean isFavorite = isFavorite(userId, club.getId());

        return RecentRecruitmentOfClubResponse.of(
                recruitment.getId(),
                recruitment.getTitle(),
                club.getName(),
                appDataS3Client.getPublicUrl(club.getLogo()),
                club.getId(),
                recruitment.getContent(),
                recruitment.getRecruitStart(),
                recruitment.getRecruitEnd(),
                RecruitStatus.from(recruitment.getRecruitStart(), recruitment.getRecruitEnd()),
                recruitment.getCreatedAt(),
                imageUrls,
                recruitment.getRecruitForm(),
                isFavorite,
                club.getInstagram(),
                club.getClubCategory().getDescription(),
                recruitment.isAlwaysRecruiting()
        );
    }


    @Transactional
    public SpecificRecruitmentResponse getSpecificRecruitment(final Long userId, final Long recruitmentId) {
        Recruitment recruitment = recruitmentRepository.findRecruitmentById(recruitmentId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUNT_RECRUITMENT));

        List<RecruitmentImage> recruitmentImages = recruitmentImageRepository.findByRecruitmentIdOrderByCreatedAtAsc(
                recruitmentId);

        List<String> imageUrls = recruitmentImages.stream()
                .map(image -> appDataS3Client.getPublicUrl(image.getImage()))
                .toList();

        Club club = recruitment.getClub();

        return SpecificRecruitmentResponse.of(
                recruitment.getId(),
                recruitment.getTitle(),
                club.getName(),
                appDataS3Client.getPublicUrl(club.getLogo()),
                club.getId(),
                recruitment.getContent(),
                recruitment.getRecruitStart(),
                recruitment.getRecruitEnd(),
                RecruitStatus.from(recruitment.getRecruitStart(), recruitment.getRecruitEnd()),
                recruitment.getCreatedAt(),
                imageUrls,
                recruitment.getRecruitForm(),
                isFavorite(userId, club.getId()),
                club.getInstagram(),
                club.getClubCategory().getDescription(),
                recruitment.isAlwaysRecruiting()
        );
    }

    @Transactional
    public AllRecruitmentResponse getAllRecruitment(final Long userId, final ClubAffiliation affiliation,
                                                    final ClubCategory category,
                                                    final Pageable pageable) {

        Page<Recruitment> recruitmentPage = recruitmentRepository.findRecruitments(affiliation, category, pageable);
        List<Recruitment> filteredRecruitments = recruitmentPage.getContent();

        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredRecruitments.size());
        List<Recruitment> pagedRecruitments = filteredRecruitments.subList(start, end);
        Page<Recruitment> paged = new PageImpl<>(pagedRecruitments, pageable, filteredRecruitments.size());

        // DTO 변환 및 정렬
        List<RecruitmentPreviewResponse> recruitmentResponses = paged.stream()
                .map(recruitment -> mapToRecruitmentPreviewResponse(userId, recruitment))
                .sorted(getFinalComparator(userId))
                .toList();

        // 페이징 정보 생성
        PageResponse pageResponse = PageResponse.of(
                paged.getNumber() + 1,
                paged.getSize(),
                paged.getTotalPages(),
                (int) paged.getTotalElements()
        );

        return new AllRecruitmentResponse(recruitmentResponses, pageResponse);
    }

    private void validateAdmin(Long userId) {
        if (userId == null) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        if (user.getRole().equals(UserRole.NORMAL)) {
            throw new MokkojiException(FailMessage.FORBIDDEN);
        }
    }

    private Recruitment buildAndSaveRecruitment(Club club, String title, String content,
                                                LocalDateTime recruitStart, LocalDateTime recruitEnd,
                                                String recruitForm, boolean isAlwaysRecruiting) {
        Recruitment recruitment = Recruitment.builder()
                .club(club)
                .title(title)
                .content(content)
                .recruitStart(recruitStart)
                .recruitEnd(recruitEnd)
                .recruitForm(recruitForm)
                .isAlwaysRecruiting(isAlwaysRecruiting)
                .build();

        recruitmentRepository.save(recruitment);
        return recruitment;
    }

    private List<String> uploadRecruitmentImages(Recruitment recruitment, List<String> imageNames) {
        List<String> presignedUrls = new ArrayList<>();

        if (imageNames.isEmpty() && imageNames == null) {
            return presignedUrls;
        }

        List<RecruitmentImage> recruitmentImages = new ArrayList<>();

        for (String imageName : imageNames) {
            String imageKey = extractImageKey(recruitment, imageName);
            String presignedPutUrl = appDataS3Client.getPresignedPutUrl(imageKey);
            presignedUrls.add(presignedPutUrl);

            RecruitmentImage recruitmentImage = RecruitmentImage.builder()
                    .recruitment(recruitment)
                    .image(imageKey)
                    .build();
            recruitmentImages.add(recruitmentImage);
        }

        recruitmentImageRepository.saveAll(recruitmentImages);
        return presignedUrls;
    }

    private String extractImageKey(Recruitment recruitment, String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        String nextDot = fileName.substring(dotIndex); //jpg와 같은 확장자 부분

        String uuid = UUID.randomUUID().toString();
        fileName = uuid + nextDot;
        Club club = recruitment.getClub();

        String imageKey = String.format("recruitment-image/%d/%d/%s", club.getId(), recruitment.getId(), fileName);
        return imageKey;
    }

    private List<String> deleteImages(Long recruitmentId) {
        List<RecruitmentImage> oldImages = recruitmentImageRepository.findByRecruitmentId(recruitmentId);

        List<String> deleteImageUrls = oldImages.stream()
                .map(image -> appDataS3Client.getPresignedDeleteUrl(image.getImage()))
                .toList();

        recruitmentImageRepository.deleteAll(oldImages);

        return deleteImageUrls;
    }

    private String getFirstImageUrl(Long recruitmentId) {
        return recruitmentImageRepository.findByRecruitmentIdOrderByCreatedAtAsc(recruitmentId).stream()
                .findFirst()
                .map(image -> appDataS3Client.getPublicUrl(image.getImage()))
                .orElse(null);
    }

    private boolean isFavorite(final Long userId, final Long clubId) {
        if (userId == null) {
            return false;
        }
        return favoriteRepository.existsByUserIdAndClubId(userId, clubId);
    }

    private RecruitmentPreviewResponse mapToRecruitmentPreviewResponse(Long userId, Recruitment recruitment) {
        boolean isFavorite = isFavorite(userId, recruitment.getClub().getId());

        ClubPreviewResponse clubPreview = new ClubPreviewResponse(
                recruitment.getClub().getId(),
                recruitment.getClub().getName(),
                recruitment.getClub().getDescription(),
                recruitment.getClub().getClubCategory(),
                recruitment.getClub().getClubAffiliation(),
                appDataS3Client.getPublicUrl(recruitment.getClub().getLogo())
        );

        return new RecruitmentPreviewResponse(
                clubPreview,
                recruitment.getId(),
                recruitment.getTitle(),
                recruitment.getRecruitStart(),
                recruitment.getRecruitEnd(),
                RecruitStatus.from(recruitment.getRecruitStart(), recruitment.getRecruitEnd()),
                isFavorite,
                recruitment.isAlwaysRecruiting()
        );
    }

    // 즐겨찾기 여부 → 모집 상태 → 마감일 순으로 정렬하는 Comparator 생성
    private Comparator<RecruitmentPreviewResponse> getFinalComparator(Long userId) {
        Comparator<RecruitmentPreviewResponse> comparator =
                Comparator.comparing(
                        (RecruitmentPreviewResponse r) ->
                                RecruitStatus.from(r.recruitStart(), r.recruitEnd()).getPriority()
                ).thenComparing(RecruitmentPreviewResponse::recruitEnd);

        if (userId != null) {
            comparator = Comparator.comparing(RecruitmentPreviewResponse::isFavorite).reversed()
                    .thenComparing(comparator);
        }

        return comparator;
    }
}
