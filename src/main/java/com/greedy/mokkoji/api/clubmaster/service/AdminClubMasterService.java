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
import com.greedy.mokkoji.enums.admin.AdminRole;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.university.UniversityCode;
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
    private final ManageAuthorizer manageAuthorizer;

    @Transactional(readOnly = true)
    public GetClubMasterApplicationsResponse getClubMasterApplications(
            final AuthRole authRole,
            final Long adminId,
            final UniversityCode universityCode,
            final ApplicationStatus status,
            final Pageable pageable
    ) {
        manageAuthorizer.validateAdminAuth(authRole, adminId);

        final Admin admin = findAdminOrThrow(adminId);

        final UniversityCode targetUniversityCode = resolveUniversityCode(admin, universityCode);
        final Page<ClubMasterApplication> page = clubMasterApplicationRepository.findByConditions(targetUniversityCode, status, pageable);

        final List<ClubMasterApplicationPreviewResponse> applications = page.getContent()
                .stream()
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

        return GetClubMasterApplicationsResponse.of(
                applications,
                PageResponse.of(page.getNumber() + 1, page.getSize(), page.getTotalPages(), (int) page.getTotalElements())
        );
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

    private Admin findAdminOrThrow(final Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_ADMIN));
    }

    private ClubMasterApplication findClubMasterApplicationOrThrow(final Long applicationId) {
        return clubMasterApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB_MASTER_APPLICATION));
    }

    private UniversityCode resolveUniversityCode(final Admin admin, final UniversityCode universityCode) {
        if (admin.getRole() == AdminRole.UNIVERSITY_ADMIN) {
            return admin.getUniversity().getCode();
        }
        return universityCode;
    }
}
