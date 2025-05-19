package com.greedy.mokkoji.api.recruitment.service;

import com.greedy.mokkoji.api.club.dto.page.PageResponse;
import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.api.recruitment.dto.request.RecruitmentCreateRequest;
import com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.RecruitmentCreateResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.SpecificRecruitmentResponse;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
            final RecruitmentCreateRequest request) {

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

        Recruitment recruitment = buildAndSaveRecruitment(club, request);
        List<String> imageUrls = uploadRecruitmentImages(recruitment, request.images());

        return RecruitmentCreateResponse.of(recruitment.getId(), imageUrls);
    }

    private Recruitment buildAndSaveRecruitment(Club club, RecruitmentCreateRequest request) {
        Recruitment recruitment = Recruitment.builder()
                .club(club)
                .title(request.title())
                .content(request.content())
                .recruitStart(request.recruitStart())
                .recruitEnd(request.recruitEnd())
                .build();

        recruitmentRepository.save(recruitment);
        return recruitment;
    }

    private List<String> uploadRecruitmentImages(Recruitment recruitment, List<String> imageKeys) {
        List<String> imageUrls = new ArrayList<>();

        if (imageKeys != null && !imageKeys.isEmpty()) {
            List<RecruitmentImage> recruitmentImages = imageKeys.stream()
                    .map(imageKey -> {
                        String presignedPutUrl = appDataS3Client.getPresignedPutUrl(imageKey);
                        imageUrls.add(presignedPutUrl);

                        return RecruitmentImage.builder()
                                .recruitment(recruitment)
                                .image(imageKey)
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

        if (recruitments.isEmpty()) {
            throw new MokkojiException(FailMessage.NOT_FOUND_USER);
        }

        List<AllRecruitmentOfClubResponse.Recruitment> recruitmentList = recruitments.stream()
                .sorted(Comparator.comparing(Recruitment::getRecruitEnd).reversed())
                .map(recruitment -> new AllRecruitmentOfClubResponse.Recruitment(
                        recruitment.getId(),
                        recruitment.getTitle(),
                        recruitment.getContent(),
                        recruitment.getRecruitStart(),
                        recruitment.getRecruitEnd(),
                        RecruitStatus.from(recruitment.getRecruitStart(), recruitment.getRecruitEnd()),
                        recruitment.getCreatedAt(),
                        getFirstImageUrl(recruitment.getId())
                ))
                .toList();

        return AllRecruitmentOfClubResponse.of(recruitmentList);
    }

    @Transactional
    public AllRecruitmentResponse getAllRecruitment(final Long userId, final Pageable pageable) {
        Page<Recruitment> recruitments = recruitmentRepository.findAll(pageable);

        List<AllRecruitmentResponse.Recruitment> recruitmentResponses = recruitments.stream()
                .map(recruitment -> mapToRecruitmentDetailResponse(userId, recruitment))
                .toList();

        if (userId != null) {
            recruitmentResponses = recruitmentResponses.stream()
                    .sorted(
                            Comparator.comparing(AllRecruitmentResponse.Recruitment::isFavorite).reversed()
                                    .thenComparing(AllRecruitmentResponse.Recruitment::recruitEnd)
                    )
                    .toList();
        } else {
            recruitmentResponses = recruitmentResponses.stream()
                    .sorted(Comparator.comparing(AllRecruitmentResponse.Recruitment::recruitEnd))
                    .toList();
        }

        PageResponse pageResponse = PageResponse.of(
                recruitments.getNumber() + 1,
                recruitments.getSize(),
                recruitments.getTotalPages(),
                (int) recruitments.getTotalElements()
        );

        return new AllRecruitmentResponse(recruitmentResponses, pageResponse);
    }


    private AllRecruitmentResponse.Recruitment mapToRecruitmentDetailResponse(Long userId, Recruitment recruitment) {
        String firstImageUrl = getFirstImageUrl(recruitment.getId());
        boolean isFavorite = isFavorite(userId, recruitment.getClub().getId());

        return new AllRecruitmentResponse.Recruitment(
                recruitment.getClub().getId(),
                recruitment.getClub().getName(),
                recruitment.getId(),
                recruitment.getTitle(),
                recruitment.getRecruitStart(),
                recruitment.getRecruitEnd(),
                RecruitStatus.from(recruitment.getRecruitStart(), recruitment.getRecruitEnd()),
                firstImageUrl,
                isFavorite
        );
    }

    private String getFirstImageUrl(Long recruitmentId) {
        return recruitmentImageRepository.findByRecruitmentIdOrderByIdAsc(recruitmentId).stream()
                .findFirst()
                .map(image -> appDataS3Client.getPresignedUrl(image.getImage()))
                .orElse(null);
    }

    private boolean isFavorite(final Long userId, final Long clubId) {
        if (userId == null) {
            return false;
        }
        return favoriteRepository.existsByUserIdAndClubId(userId, clubId);
    }
}
