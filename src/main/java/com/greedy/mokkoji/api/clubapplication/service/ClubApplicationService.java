package com.greedy.mokkoji.api.clubapplication.service;

import com.greedy.mokkoji.api.clubapplication.dto.request.ClubApplicationCreateRequest;
import com.greedy.mokkoji.api.clubapplication.dto.response.ClubApplicationCreateResponse;
import com.greedy.mokkoji.api.clubapplication.dto.response.ClubApplicationResponse;
import com.greedy.mokkoji.api.clubapplication.dto.response.ClubApplicationsResponse;
import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.clubapplication.entity.ClubApplication;
import com.greedy.mokkoji.db.clubapplication.repository.ClubApplicationRepository;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.university.repository.UniversityRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubApplicationService {

    private final ClubApplicationRepository clubApplicationRepository;
    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final AppDataS3Client appDataS3Client;

    @Transactional
    public ClubApplicationCreateResponse createClubApplication(final Long userId, final ClubApplicationCreateRequest request) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        final University university = universityRepository.findByCode(request.universityCode())
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_UNIVERSITY));

        final String clubName = request.clubName();

        if (clubApplicationRepository.existsByApplicantAndUniversityAndClubNameAndStatusNot(user, university, clubName, ApplicationStatus.REJECTED)) {
            throw new MokkojiException(FailMessage.CONFLICT_CLUB_APPLICATION);
        }

        final ClubApplication clubApplication = ClubApplication.builder()
                .university(university)
                .applicant(user)
                .applicantName(request.applicantName())
                .clubName(clubName)
                .clubCategory(request.clubCategory())
                .clubAffiliation(request.clubAffiliation())
                .logo(null)
                .instagram(request.instagram())
                .description(request.description())
                .build();

        clubApplicationRepository.save(clubApplication);

        final String logoKey = extractLogoKey(clubApplication.getId(), request.logo());
        clubApplication.updateLogo(logoKey);

        final String uploadLogoUrl = appDataS3Client.getPresignedPutUrl(logoKey);

        return ClubApplicationCreateResponse.of(clubApplication.getId(), uploadLogoUrl);
    }

    @Nullable
    private String extractLogoKey(final Long applicationId, final String logo) {
        if (logo == null || logo.isBlank()) {
            return null;
        }

        final int dotIndex = logo.lastIndexOf('.');
        final String prevDot = logo.substring(0, dotIndex);
        final String nextDot = logo.substring(dotIndex);
        final String uuid = UUID.randomUUID().toString();
        final String fileName = prevDot + "_" + uuid + nextDot;

        return String.format("club-application-logo/%d/%s", applicationId, fileName);
    }

    @Transactional(readOnly = true)
    public ClubApplicationsResponse getMyClubApplications(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        final List<ClubApplicationResponse> clubApplications = clubApplicationRepository.findByApplicant(user)
                .stream()
                .map(ClubApplicationResponse::from)
                .toList();

        return ClubApplicationsResponse.of(clubApplications);
    }

}
