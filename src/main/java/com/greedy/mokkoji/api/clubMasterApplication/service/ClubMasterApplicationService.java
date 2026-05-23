package com.greedy.mokkoji.api.clubMasterApplication.service;

import com.greedy.mokkoji.api.clubMasterApplication.dto.response.GetClubMasterApplicationsResponse;
import com.greedy.mokkoji.api.clubMasterApplication.dto.response.GetMyClubMasterApplicationsResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.clubMasterApplication.entity.ClubMasterApplication;
import com.greedy.mokkoji.db.clubMasterApplication.repository.ClubMasterApplicationRepository;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.university.repository.UniversityRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.university.UniversityCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMasterApplicationService {

    private final ClubMasterApplicationRepository clubMasterApplicationRepository;
    private final UniversityRepository universityRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createClubMasterApplication(
            final Long userId,
            final UniversityCode universityCode,
            final Long clubId,
            final String userName
    ) {
        University university = getUniversityOrThrow(universityCode);
        Club club = getClubOrThrow(clubId);
        User user = getUserOrThrow(userId);

        user.updateName(userName);

        ClubMasterApplication application = ClubMasterApplication.builder()
                .university(university)
                .club(club)
                .user(user)
                .userName(userName)
                .build();

        clubMasterApplicationRepository.save(application);
    }

    @Transactional
    public List<GetMyClubMasterApplicationsResponse> getMyClubMasterApplications(
            final Long userId
    ) {
        User user = getUserOrThrow(userId);
        List<ClubMasterApplication> applications = clubMasterApplicationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return applications.stream()
                .map(application -> new GetMyClubMasterApplicationsResponse(
                        application.getId(),
                        application.getUniversity().getName(),
                        application.getClub().getName(),
                        application.getUserName(),
                        application.getStatus(),
                        application.getRejectReason(),
                        application.getCreatedAt(),
                        application.getUpdatedAt()
                ))
                .toList();
    }

    @Transactional
    public List<GetClubMasterApplicationsResponse> getClubMasterApplications(
            final AuthRole authRole,
            final Long userId
    ) {
        validateAdminRole(authRole);

        User user = getUserOrThrow(userId);
        Long universityId = user.getUniversity().getId();

        List<ClubMasterApplication> applications = clubMasterApplicationRepository.findByUniversityIdOrderByCreatedAtAsc(universityId);

        return applications.stream()
                .map(application -> new GetClubMasterApplicationsResponse(
                        application.getId(),
                        application.getUniversity().getName(),
                        application.getClub().getName(),
                        application.getUserName(),
                        application.getStatus(),
                        application.getRejectReason(),
                        application.getCreatedAt(),
                        application.getUpdatedAt()
                ))
                .toList();
    }

    private University getUniversityOrThrow(final UniversityCode universityCode) {
        return universityRepository.findByUniversityCode(universityCode)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_UNIVERSITY));
    }

    private Club getClubOrThrow(final Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB));
    }

    private User getUserOrThrow(final Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
    }

    private void validateAdminRole(AuthRole authRole) {
        if (!AuthRole.ADMIN.equals(authRole)) {
            throw new MokkojiException(FailMessage.FORBIDDEN_MANAGE_UNIVERSITY_CLUB);
        }
    }
}
