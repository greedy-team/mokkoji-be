package com.greedy.mokkoji.api.recruitment.service;

import com.greedy.mokkoji.api.recruitment.dto.RecruitmentCreateRequest;
import com.greedy.mokkoji.api.recruitment.dto.RecruitmentCreateResponse;
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
import com.greedy.mokkoji.enums.user.UserRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        if (user.getRole() == UserRole.NORMAL) {
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
                .recruitForm("기본폼") // recruitForm 필드 채워주세요
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

        return RecruitmentCreateResponse.builder()
                .recruitment(recruitment)
                .images(savedImageUrls)
                .build();
    }

}
