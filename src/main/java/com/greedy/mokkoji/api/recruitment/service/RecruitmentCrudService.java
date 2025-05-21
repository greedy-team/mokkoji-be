package com.greedy.mokkoji.api.recruitment.service;

import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.api.recruitment.dto.response.createRecruitment.CreateRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.deleteRecruitment.DeleteRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.updateRecruitment.UpdateRecruitmentResponse;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecruitmentCrudService {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentImageRepository recruitmentImageRepository;
    private final AppDataS3Client appDataS3Client;

    @Transactional
    public CreateRecruitmentResponse createRecruitment(
            final Long userId,
            final Long clubId,
            final String title,
            final String content,
            final LocalDateTime recruitStart,
            final LocalDateTime recruitEnd,
            final List<String> images,
            final String recruitForm) {

        validateAdmin(userId);

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB));

        Recruitment recruitment = buildAndSaveRecruitment(club, title, content, recruitStart, recruitEnd, recruitForm);
        List<String> uploadImageUrls = uploadRecruitmentImages(recruitment, images);

        return CreateRecruitmentResponse.of(recruitment.getId(), uploadImageUrls);
    }

    private Recruitment buildAndSaveRecruitment(Club club, String title, String content,
                                                LocalDateTime recruitStart, LocalDateTime recruitEnd, String recruitForm) {
        Recruitment recruitment = Recruitment.builder()
                .club(club)
                .title(title)
                .content(content)
                .recruitStart(recruitStart)
                .recruitEnd(recruitEnd)
                .recruitForm(recruitForm)
                .build();

        recruitmentRepository.save(recruitment);
        return recruitment;
    }

    @Transactional
    public UpdateRecruitmentResponse updateRecruitment(
            final Long userId,
            final Long recruitmentId,
            final String title,
            final String content,
            final LocalDateTime recruitStart,
            final LocalDateTime recruitEnd,
            final List<String> newImages,
            final String recruitForm
    ) {
        validateAdmin(userId);

        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUNT_RECRUITMENT));

        recruitment.updateRecruitment(title, content, recruitStart, recruitEnd, recruitForm);

        List<String> deleteImageUrls = deleteImages(recruitment.getId());

        List<String> uploadImageUrls = uploadRecruitmentImages(recruitment, newImages);

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

    private List<String> deleteImages(Long recruitmentId) {
        List<RecruitmentImage> oldImages = recruitmentImageRepository.findByRecruitmentId(recruitmentId);

        List<String> deleteImageUrls = oldImages.stream()
                .map(image -> appDataS3Client.getPresignedDeleteUrl(image.getImage()))
                .toList();

        recruitmentImageRepository.deleteAll(oldImages);

        return deleteImageUrls;
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

    private void validateAdmin(Long userId) {
        if (userId == null) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        //Todo: 권한 설정에 대해서 추가적으로 이야기 해보기
        if (user.getRole().equals(UserRole.NORMAL)) {
            throw new MokkojiException(FailMessage.FORBIDDEN);
        }
    }
}
