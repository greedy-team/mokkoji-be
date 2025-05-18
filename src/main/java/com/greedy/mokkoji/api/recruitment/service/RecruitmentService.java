package com.greedy.mokkoji.api.recruitment.service;

import com.greedy.mokkoji.api.recruitment.dto.request.RecruitmentCreateRequest;
import com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.RecruitmentCreateResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.SpecificRecruitmentResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
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
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentService {
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentImageRepository recruitmentImageRepository;


    @Transactional
    public RecruitmentCreateResponse createResponse(final Long userId, final Long clubId, final RecruitmentCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        if (user.getRole().equals(UserRole.NORMAL)) {
            throw new MokkojiException(FailMessage.FORBIDDEN);
        }

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB));

        Recruitment recruitment = Recruitment.builder()
                .club(club)
                .title(request.title())
                .content(request.content())
                .recruitStart(request.startDate())
                .recruitEnd(request.endDate())
                .build();

        recruitmentRepository.save(recruitment);

        List<RecruitmentImage> recruitmentImages = request.images().stream()
                .map(image -> RecruitmentImage.builder()
                        .recruitment(recruitment)
                        .image(image)
                        .build())
                .toList();

        recruitmentImageRepository.saveAll(recruitmentImages);

        List<String> savedImageUrls = recruitmentImages.stream()
                .map(RecruitmentImage::getImage)
                .toList();

        return RecruitmentCreateResponse.of(recruitment, savedImageUrls);
    }

    @Transactional
    public AllRecruitmentOfClubResponse getAllRecruitmentOfClub(final Long userId, final Long clubId) {
        List<Recruitment> recruitments = recruitmentRepository.findAllByClubId(clubId);

        if (recruitments.isEmpty()) {
            throw new MokkojiException(FailMessage.NOT_FOUND_USER);
        }

        List<AllRecruitmentOfClubResponse.Recruitment> recruitmentList = recruitments.stream()
                .sorted(Comparator.comparing(Recruitment::getRecruitEnd).reversed())
                .map(recruitment -> new AllRecruitmentOfClubResponse.Recruitment(
                        recruitment.getId(),
                        recruitment.getTitle(),
                        RecruitStatus.from(recruitment.getRecruitStart(), recruitment.getRecruitEnd()),
                        recruitment.getCreatedAt()
                ))
                .toList();

        return AllRecruitmentOfClubResponse.of(recruitmentList);
    }

    @Transactional
    public SpecificRecruitmentResponse getSpecificRecruitment(final Long userId, final Long recruitmentId) {
        Recruitment recruitment = recruitmentRepository.findRecruitmentById(recruitmentId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUNT_RECRUITMENT));

        List<RecruitmentImage> recruitmentImages = recruitmentImageRepository.findByRecruitmentIdOrderByIdAsc(recruitmentId);

        List<String> imageUrls = recruitmentImages.stream()
                .map(RecruitmentImage::getImage)
                .toList();

        return SpecificRecruitmentResponse.of(
                recruitment.getId(),
                recruitment.getTitle(),
                imageUrls,
                recruitment.getContent(),
                recruitment.getRecruitStart().toLocalDate(),
                recruitment.getRecruitEnd().toLocalDate()
        );
    }

    @Transactional
    public AllRecruitmentResponse getAllRecruitment(final Long userId) {
        List<Recruitment> recruitments = recruitmentRepository.findAll();

        List<AllRecruitmentResponse.Recruitment> responseList = recruitments.stream()
                .map(r -> new AllRecruitmentResponse.Recruitment(
                        r.getClub().getId(),
                        r.getId(),
                        r.getTitle(),
                        RecruitStatus.from(r.getRecruitStart(), r.getRecruitEnd()),
                        r.getRecruitStart().toLocalDate(),
                        r.getRecruitEnd().toLocalDate(),
                        r.getClub().getName()
                ))
                .toList();

        return AllRecruitmentResponse.of(responseList);
    }
}
