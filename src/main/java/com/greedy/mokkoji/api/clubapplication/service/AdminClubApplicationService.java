package com.greedy.mokkoji.api.clubapplication.service;

import com.greedy.mokkoji.api.auth.service.ManageAuthorizer;
import com.greedy.mokkoji.api.clubapplication.dto.response.AdminClubApplicationResponse;
import com.greedy.mokkoji.api.clubapplication.dto.response.AdminClubApplicationsResponse;
import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.admin.entity.Admin;
import com.greedy.mokkoji.db.admin.repository.AdminRepository;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.clubapplication.entity.ClubApplication;
import com.greedy.mokkoji.db.clubapplication.repository.ClubApplicationRepository;
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
public class AdminClubApplicationService {

    private final ClubApplicationRepository clubApplicationRepository;
    private final ClubRepository clubRepository;
    private final AdminRepository adminRepository;
    private final ManageAuthorizer manageAuthorizer;

    @Transactional(readOnly = true)
    public AdminClubApplicationsResponse getAdminClubApplications(
            final AuthRole authRole,
            final Long adminId,
            final UniversityCode universityCode,
            final ApplicationStatus status,
            final Pageable pageable
    ) {
        manageAuthorizer.validateAdminAuth(authRole, adminId);

        final Admin admin = findAdminOrThrow(adminId);
        final UniversityCode targetUniversityCode = resolveUniversityCode(admin, universityCode);

        final Page<ClubApplication> page = clubApplicationRepository.findByConditions(targetUniversityCode, status, pageable);

        final List<AdminClubApplicationResponse> applications = page.getContent()
                .stream()
                .map(AdminClubApplicationResponse::from)
                .toList();

        return AdminClubApplicationsResponse.of(
                applications,
                PageResponse.of(page.getNumber(), page.getSize(), page.getTotalPages(), (int) page.getTotalElements())
        );
    }

    @Transactional
    public void approveClubApplication(final AuthRole authRole, final Long adminId, final Long applicationId) {
        manageAuthorizer.validateAdminAuth(authRole, adminId);
        final ClubApplication clubApplication = findClubApplicationOrThrow(applicationId);
        manageAuthorizer.validateCanManageUniversity(adminId, clubApplication.getUniversity());

        if (clubApplication.getStatus() != ApplicationStatus.PENDING) {
            throw new MokkojiException(FailMessage.CONFLICT_APPLICATION_STATUS);
        }

        final User applicant = clubApplication.getApplicant();
        applicant.updateRole(UserRole.CLUB_MASTER);

        final Club club = Club.builder()
                .name(clubApplication.getClubName())
                .university(clubApplication.getUniversity())
                .clubCategory(clubApplication.getClubCategory())
                .clubAffiliation(clubApplication.getClubAffiliation())
                .logo(clubApplication.getLogo())
                .instagram(clubApplication.getInstagram())
                .description(clubApplication.getDescription())
                .master(applicant)
                .build();

        clubRepository.save(club);
        clubApplication.approve();
    }

    @Transactional
    public void rejectClubApplication(final AuthRole authRole, final Long adminId, final Long applicationId, final String rejectReason) {
        manageAuthorizer.validateAdminAuth(authRole, adminId);
        final ClubApplication clubApplication = findClubApplicationOrThrow(applicationId);
        manageAuthorizer.validateCanManageUniversity(adminId, clubApplication.getUniversity());

        if (clubApplication.getStatus() != ApplicationStatus.PENDING) {
            throw new MokkojiException(FailMessage.CONFLICT_APPLICATION_STATUS);
        }

        clubApplication.reject(rejectReason);
    }

    private UniversityCode resolveUniversityCode(final Admin admin, final UniversityCode universityCode) {
        if (admin.getRole() == AdminRole.UNIVERSITY_ADMIN) {
            return admin.getUniversity().getCode();
        }
        return universityCode;
    }

    private Admin findAdminOrThrow(final Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_ADMIN));
    }

    private ClubApplication findClubApplicationOrThrow(final Long applicationId) {
        return clubApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB_APPLICATION));
    }
}
