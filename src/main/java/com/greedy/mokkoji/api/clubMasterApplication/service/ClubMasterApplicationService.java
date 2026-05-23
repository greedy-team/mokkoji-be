package com.greedy.mokkoji.api.clubMasterApplication.service;

import com.greedy.mokkoji.api.clubMasterApplication.dto.response.GetMyClubMasterApplicationResponse;
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
    public List<GetMyClubMasterApplicationResponse> getMyClubMasterApplication(
            final Long userId
    ) {
        User user = getUserOrThrow(userId);
        List<ClubMasterApplication> applications = clubMasterApplicationRepository.findByUserId(user.getId());

        return applications.stream()
                .map(application -> new GetMyClubMasterApplicationResponse(
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

}
