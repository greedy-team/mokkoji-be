package com.greedy.mokkoji.api.recruitment.service;

import com.greedy.mokkoji.api.club.dto.page.PageResponse;
import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitment.AllRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitment.ClubPreviewResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitment.RecruitmentPreviewResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub.AllRecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub.RecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.recruitmentCreate.RecruitmentCreateResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.specificRecruitment.SpecificRecruitmentResponse;
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
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import com.greedy.mokkoji.enums.user.UserRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentImageRepository recruitmentImageRepository;
    private final FavoriteRepository favoriteRepository;
    private final AppDataS3Client appDataS3Client;

    @Transactional
    public RecruitmentCreateResponse createRecruitment(
            final Long userId,
            final Long clubId,
            final String title,
            final String content,
            final LocalDateTime recruitStart,
            final LocalDateTime recruitEnd,
            final List<String> images) {

        if (userId == null) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        if (user.getRole().equals(UserRole.NORMAL)) {
            throw new MokkojiException(FailMessage.FORBIDDEN);
        }

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB));

        Recruitment recruitment = buildAndSaveRecruitment(club, title, content, recruitStart, recruitEnd);
        List<String> imageUrls = uploadRecruitmentImages(recruitment, images);

        return RecruitmentCreateResponse.of(recruitment.getId(), imageUrls);
    }

    private Recruitment buildAndSaveRecruitment(Club club, String title, String content,
                                                LocalDateTime recruitStart, LocalDateTime recruitEnd) {
        Recruitment recruitment = Recruitment.builder()
                .club(club)
                .title(title)
                .content(content)
                .recruitStart(recruitStart)
                .recruitEnd(recruitEnd)
                .build();

        recruitmentRepository.save(recruitment);
        return recruitment;
    }

    private List<String> uploadRecruitmentImages(Recruitment recruitment, List<String> imageKeys) {
        List<String> imageUrls = new ArrayList<>();

        if (imageKeys != null && !imageKeys.isEmpty()) {
            //ex) "Greedy Club" -> "greedy-club"
            String clubName = recruitment.getClub().getName().replaceAll("\\s+", "-").toLowerCase();

            List<RecruitmentImage> recruitmentImages = imageKeys.stream()
                    .map(originalName -> {

                        //확장자 추출
                        String extension = "";
                        int dotIndex = originalName.lastIndexOf('.');
                        if (dotIndex != -1 && dotIndex < originalName.length() - 1) {
                            extension = originalName.substring(dotIndex);
                        }

                        //uniqueKey 생성
                        String uniqueKey = "recruitment/" + clubName + "/" + UUID.randomUUID() + extension;

                        String presignedPutUrl = appDataS3Client.getPresignedPutUrl(uniqueKey);
                        imageUrls.add(presignedPutUrl);

                        return RecruitmentImage.builder()
                                .recruitment(recruitment)
                                .image(uniqueKey)
                                .build();
                    })

                    .toList();

            recruitmentImageRepository.saveAll(recruitmentImages);
        }

        return imageUrls;
    }

    @Transactional
    public SpecificRecruitmentResponse getSpecificRecruitment(final Long recruitmentId) {
        Recruitment recruitment = recruitmentRepository.findRecruitmentById(recruitmentId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUNT_RECRUITMENT));

        List<RecruitmentImage> recruitmentImages = recruitmentImageRepository.findByRecruitmentIdOrderByIdAsc(recruitmentId);

        List<String> imageUrls = recruitmentImages.stream()
                .map(image -> appDataS3Client.getPresignedUrl(image.getImage()))
                .toList();

        return SpecificRecruitmentResponse.of(
                recruitment.getId(),
                recruitment.getTitle(),
                recruitment.getContent(),
                recruitment.getRecruitStart(),
                recruitment.getRecruitEnd(),
                RecruitStatus.from(recruitment.getRecruitStart(), recruitment.getRecruitEnd()),
                recruitment.getCreatedAt(),
                imageUrls,
                recruitment.getRecruitForm()
        );
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
                        .build()
                )
                .toList();

        return AllRecruitmentOfClubResponse.of(recruitmentList);
    }


    private String getFirstImageUrl(Long recruitmentId) {
        return recruitmentImageRepository.findByRecruitmentIdOrderByIdAsc(recruitmentId).stream()
                .findFirst()
                .map(image -> appDataS3Client.getPresignedUrl(image.getImage()))
                .orElse(null);
    }

    @Transactional
    public AllRecruitmentResponse getAllRecruitment(final Long userId, final Pageable pageable) {
        Page<Recruitment> recruitments = recruitmentRepository.findAll(pageable);

        List<RecruitmentPreviewResponse> recruitmentResponses = recruitments.stream()
                .map(recruitment -> mapToRecruitmentPreviewResponse(userId, recruitment))
                .sorted(getFinalComparator(userId))
                .toList();

        PageResponse pageResponse = PageResponse.of(
                recruitments.getNumber() + 1,
                recruitments.getSize(),
                recruitments.getTotalPages(),
                (int) recruitments.getTotalElements()
        );

        return new AllRecruitmentResponse(recruitmentResponses, pageResponse);
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

    // 모집 상태를 숫자로 매핑하여 우선순위 정렬에 사용
    private int getRecruitStatusOrder(RecruitStatus status) {
        return switch (status) {
            case IMMINENT -> 0;
            case OPEN -> 1;
            case BEFORE -> 2;
            case CLOSED -> 3;
        };
    }

    private RecruitmentPreviewResponse mapToRecruitmentPreviewResponse(Long userId, Recruitment recruitment) {
        boolean isFavorite = isFavorite(userId, recruitment.getClub().getId());

        ClubPreviewResponse clubPreview = new ClubPreviewResponse(
                recruitment.getClub().getId(),
                recruitment.getClub().getName(),
                recruitment.getClub().getDescription(),
                recruitment.getClub().getClubCategory(),
                recruitment.getClub().getClubAffiliation(),
                appDataS3Client.getPresignedUrl(recruitment.getClub().getLogo())
        );

        return new RecruitmentPreviewResponse(
                clubPreview,
                recruitment.getId(),
                recruitment.getTitle(),
                recruitment.getRecruitStart(),
                recruitment.getRecruitEnd(),
                RecruitStatus.from(recruitment.getRecruitStart(), recruitment.getRecruitEnd()),
                isFavorite
        );
    }

    private boolean isFavorite(final Long userId, final Long clubId) {
        if (userId == null) {
            return false;
        }
        return favoriteRepository.existsByUserIdAndClubId(userId, clubId);
    }
}
