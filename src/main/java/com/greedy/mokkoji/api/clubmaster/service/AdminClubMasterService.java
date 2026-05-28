package com.greedy.mokkoji.api.clubmaster.service;

import com.greedy.mokkoji.api.admin.dto.response.ClubMasterApplicationPreviewResponse;
import com.greedy.mokkoji.api.admin.dto.response.GetClubMasterApplicationsResponse;
import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.admin.entity.Admin;
import com.greedy.mokkoji.db.admin.repository.AdminRepository;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.clubmaster.entity.ClubMasterApplication;
import com.greedy.mokkoji.db.clubmaster.repository.ClubMasterApplicationRepository;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
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
public class AdminClubMasterService {
    private final ClubMasterApplicationRepository clubMasterApplicationRepository;
    private final AdminRepository adminRepository;

    @Transactional
    public GetClubMasterApplicationsResponse getClubMasterApplications(
            final AuthRole authRole,
            final Long userId,
            final Pageable pageable
    ) {
        validateAdminRole(authRole);

        Admin admin = findAdminOrThrow(userId);
        Long universityId = admin.getUniversityId();

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

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new MokkojiException(FailMessage.CONFLICT_APPLICATION_STATUS);
        }

        Admin admin = findAdminOrThrow(userId);

        validateAdmin(admin, application.getUniversityId());
        Club club = application.getClub();

        application.approve(club, application.getUser());
    }

    public void rejectClubMasterApplication(
            final AuthRole authRole,
            final Long userId,
            final Long applicationId,
            final String rejectReason
    ) {
        validateAdminRole(authRole);

        ClubMasterApplication application = findClubMasterApplicationOrThrow(applicationId);
        Admin admin = findAdminOrThrow(userId);

        validateAdmin(admin, application.getUniversityId());

        application.reject(rejectReason);
    }

    private void validateAdminRole(AuthRole authRole) {
        if (!AuthRole.ADMIN.equals(authRole)) {
            throw new MokkojiException(FailMessage.FORBIDDEN_MANAGE_UNIVERSITY_CLUB);
        }
    }

    private void validateAdmin(Admin admin, Long universityId) {
        if (universityId != null && !universityId.equals(admin.getUniversityId())) {
            throw new MokkojiException(FailMessage.FORBIDDEN_MANAGE_THIS_UNIVERSITY_CLUB);
        }
    }

    private Admin findAdminOrThrow(final Long userId) {
        return adminRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_ADMIN));
    }

    private ClubMasterApplication findClubMasterApplicationOrThrow(final Long applicationId) {
        return clubMasterApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB_MASTER_APPLICATION));
    }
}
