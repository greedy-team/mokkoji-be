package com.greedy.mokkoji.api.recruitment.service;

import com.greedy.mokkoji.api.club.dto.page.PageResponse;
import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitment.AllRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitment.ClubPreviewResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitment.RecruitmentPreviewResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub.AllRecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub.RecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.specificRecruitment.SpecificRecruitmentResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.entity.RecruitmentImage;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentImageRepository;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruitmentGetService {
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentImageRepository recruitmentImageRepository;
    private final FavoriteRepository favoriteRepository;
    private final AppDataS3Client appDataS3Client;


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
        List<Recruitment> allRecruitments = recruitmentRepository.findAll();

        // 마감기한 가장 늦은 글만 필터링 (동아리별 하나씩)
        List<Recruitment> filteredRecruitments = filterLatestRecruitmentPerClub(allRecruitments);

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

    private List<Recruitment> filterLatestRecruitmentPerClub(List<Recruitment> recruitments) {
        return recruitments.stream()
                .collect(Collectors.toMap(
                        r -> r.getClub().getId(), r -> r,
                        (r1, r2) -> r1.getRecruitEnd().isAfter(r2.getRecruitEnd()) ? r1 : r2
                ))
                .values()
                .stream()
                .toList();
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
