package com.greedy.mokkoji.api.clubmaster.service;

import com.greedy.mokkoji.api.admin.dto.response.ClubMasterApplicationPreviewResponse;
import com.greedy.mokkoji.api.admin.dto.response.GetClubMasterApplicationsResponse;
import com.greedy.mokkoji.api.auth.service.ManageAuthorizer;
import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.admin.entity.Admin;
import com.greedy.mokkoji.db.admin.repository.AdminRepository;
import com.greedy.mokkoji.db.clubmaster.entity.ClubMasterApplication;
import com.greedy.mokkoji.db.clubmaster.repository.ClubMasterApplicationRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminClubMasterService {
    private final ClubMasterApplicationRepository clubMasterApplicationRepository;
    private final AdminRepository adminRepository;
    private ManageAuthorizer manageAuthorizer;

    @Transactional(readOnly = true)
    public GetClubMasterApplicationsResponse getClubMasterApplications(
            final AuthRole authRole,
            final Long userId,
            final Pageable pageable
    ) {
        manageAuthorizer.validateAdminAuth(authRole, userId);

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
        manageAuthorizer.validateAdminAuth(authRole, userId);
        ClubMasterApplication application = findClubMasterApplicationOrThrow(applicationId);
        manageAuthorizer.validateCanManageUniversity(userId, application.getUniversity());

        application.approve();

        User applicant = application.getUser();
        application.getClub().assignMaster(applicant);
        applicant.updateRole(UserRole.CLUB_MASTER);
    }

    @Transactional
    public void rejectClubMasterApplication(
            final AuthRole authRole,
            final Long userId,
            final Long applicationId,
            final String rejectReason
    ) {
        manageAuthorizer.validateAdminAuth(authRole, userId);
        ClubMasterApplication application = findClubMasterApplicationOrThrow(applicationId);
        manageAuthorizer.validateCanManageUniversity(userId, application.getUniversity());

        application.reject(rejectReason);
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
