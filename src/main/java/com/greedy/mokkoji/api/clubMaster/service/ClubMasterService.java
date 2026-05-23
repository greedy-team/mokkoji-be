package com.greedy.mokkoji.api.clubMaster.service;

import com.greedy.mokkoji.api.clubMaster.dto.response.GetMyClubMasterApplicationsResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.clubMasterApplication.entity.ClubMasterApplication;
import com.greedy.mokkoji.db.clubMasterApplication.repository.ClubMasterApplicationRepository;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.university.repository.UniversityRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.university.UniversityCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMasterService {

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
        University university = findUniversityOrThrow(universityCode);

        Club club = findClubOrThrow(clubId);
        if (club.getMaster() != null) {
            throw new MokkojiException(FailMessage.FORBIDDEN_ALREADY_EXIST_CLUB_MASTER);
        }

        User user = findUserOrThrow(userId);
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
        User user = findUserOrThrow(userId);
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

    private University findUniversityOrThrow(final UniversityCode universityCode) {
        return universityRepository.findByUniversityCode(universityCode)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_UNIVERSITY));
    }

    private Club findClubOrThrow(final Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB));
    }

    private User findUserOrThrow(final Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
    }
}
