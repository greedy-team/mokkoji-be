package com.greedy.mokkoji.api.admin.service;

import com.greedy.mokkoji.api.admin.dto.response.ClubMasterApplicationPreviewResponse;
import com.greedy.mokkoji.api.admin.dto.response.GetClubMasterApplicationsResponse;
import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.clubmaster.entity.ClubMasterApplication;
import com.greedy.mokkoji.db.clubmaster.repository.ClubMasterApplicationRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {
    private final ClubMasterApplicationRepository clubMasterApplicationRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;

    @Transactional
    public GetClubMasterApplicationsResponse getClubMasterApplications(
            final AuthRole authRole,
            final Long userId,
            final Pageable pageable
    ) {
        validateAdminRole(authRole);

        User user = findUserOrThrow(userId);
        Long universityId = user.getUniversity().getId();

        Page<ClubMasterApplication> applicationPage = (universityId == null)
                ? clubMasterApplicationRepository.findAllByOrderByCreatedAtAsc(pageable)
                : clubMasterApplicationRepository.findByUniversityIdOrderByCreatedAtAsc(universityId, pageable);

        List<ClubMasterApplicationPreviewResponse> applications = applicationPage.getContent().stream()
                .map(application -> new ClubMasterApplicationPreviewResponse(
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

        PageResponse pageResponse = PageResponse.of(
                applicationPage.getNumber() + 1,
                applicationPage.getSize(),
                applicationPage.getTotalPages(),
                (int) applicationPage.getTotalElements()
        );

        return GetClubMasterApplicationsResponse.of(applications, pageResponse);
    }

    @Transactional
    public void approveClubMasterApplication(
            final AuthRole authRole,
            final Long userId,
            final Long applicationId
    ) {
        validateAdminRole(authRole);

        ClubMasterApplication application = findClubMasterApplicationOrThrow(applicationId);
        User user = findUserOrThrow(userId);

        validateAdmin(user, application.getUniversityId());
        Club club = application.getClub();

        application.approve(club, user);
    }

    public void rejectClubMasterApplication(
            final AuthRole authRole,
            final Long userId,
            final Long applicationId,
            final String rejectReason
    ) {
        validateAdminRole(authRole);

        ClubMasterApplication application = findClubMasterApplicationOrThrow(applicationId);
        User user = findUserOrThrow(userId);

        validateAdmin(user, application.getUniversityId());

        application.reject(rejectReason);
    }

    private void validateAdminRole(AuthRole authRole) {
        if (!AuthRole.ADMIN.equals(authRole)) {
            throw new MokkojiException(FailMessage.FORBIDDEN_MANAGE_UNIVERSITY_CLUB);
        }
    }

    private void validateAdmin(User user, Long universityId) {
        if (universityId != null && !universityId.equals(user.getUniversity().getId())) {
            throw new MokkojiException(FailMessage.FORBIDDEN_MANAGE_THIS_UNIVERSITY_CLUB);
        }
    }

    private User findUserOrThrow(final Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
    }

    private ClubMasterApplication findClubMasterApplicationOrThrow(final Long applicationId) {
        return clubMasterApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB_MASTER_APPLICATION));
    }
}
