package com.greedy.mokkoji.api.clubapplication.service;

import com.greedy.mokkoji.api.clubapplication.dto.request.ClubApplicationCreateRequest;
import com.greedy.mokkoji.api.clubapplication.dto.response.ClubApplicationResponse;
import com.greedy.mokkoji.api.clubapplication.dto.response.ClubApplicationsResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubApplicationService {

    private final ClubApplicationRepository clubApplicationRepository;
    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;

    @Transactional
    public void createClubApplication(final Long userId, final ClubApplicationCreateRequest request) {
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
                .logo(request.logo())
                .instagram(request.instagram())
                .description(request.description())
                .build();

        clubApplicationRepository.save(clubApplication);
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
