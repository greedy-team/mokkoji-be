package com.greedy.mokkoji.api.admin.service;

import com.greedy.mokkoji.api.admin.dto.response.GetClubMasterApplicationsResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.clubMasterApplication.entity.ClubMasterApplication;
import com.greedy.mokkoji.db.clubMasterApplication.repository.ClubMasterApplicationRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {
    private final ClubMasterApplicationRepository clubMasterApplicationRepository;
    private final UserRepository userRepository;

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
