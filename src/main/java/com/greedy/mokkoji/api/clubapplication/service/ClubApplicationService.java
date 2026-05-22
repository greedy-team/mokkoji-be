package com.greedy.mokkoji.api.clubapplication.service;

import com.greedy.mokkoji.api.clubapplication.dto.request.ClubApplicationCreateRequest;
import com.greedy.mokkoji.api.clubapplication.dto.response.AdminClubApplicationResponse;
import com.greedy.mokkoji.api.clubapplication.dto.response.AdminClubApplicationsResponse;
import com.greedy.mokkoji.api.clubapplication.dto.response.ClubApplicationResponse;
import com.greedy.mokkoji.api.clubapplication.dto.response.ClubApplicationsResponse;
import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.admin.entity.Admin;
import com.greedy.mokkoji.db.admin.repository.AdminRepository;
import com.greedy.mokkoji.db.clubapplication.entity.ClubApplication;
import com.greedy.mokkoji.db.clubapplication.repository.ClubApplicationRepository;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.university.repository.UniversityRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.admin.AdminRole;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.clubApplication.ClubApplicationStatus;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.university.UniversityCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubApplicationService {

    private final ClubApplicationRepository clubApplicationRepository;
    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final AdminRepository adminRepository;

    @Transactional
    public void createClubApplication(final Long userId, final ClubApplicationCreateRequest request) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        final University university = universityRepository.findByCode(request.universityCode())
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_UNIVERSITY));

        if (clubApplicationRepository.existsByApplicantAndUniversityAndStatusNot(user, university, ClubApplicationStatus.REJECTED)) {
            throw new MokkojiException(FailMessage.CONFLICT_CLUB_APPLICATION);
        }

        final ClubApplication clubApplication = ClubApplication.builder()
                .university(university)
                .applicant(user)
                .applicantName(request.applicantName())
                .clubName(request.clubName())
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

    @Transactional(readOnly = true)
    public AdminClubApplicationsResponse getAdminClubApplications(
            final AuthRole authRole,
            final Long adminId,
            final UniversityCode universityCode,
            final ClubApplicationStatus status,
            final Pageable pageable
    ) {
        if (!AuthRole.ADMIN.equals(authRole)) {
            throw new MokkojiException(FailMessage.FORBIDDEN);
        }

        final Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

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

    private UniversityCode resolveUniversityCode(final Admin admin, final UniversityCode universityCode) {
        if (admin.getRole() == AdminRole.UNIVERSITY_ADMIN) {
            return admin.getUniversity().getCode();
        }
        return universityCode;
    }
}
