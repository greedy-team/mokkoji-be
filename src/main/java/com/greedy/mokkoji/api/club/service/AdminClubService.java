package com.greedy.mokkoji.api.club.service;

import com.greedy.mokkoji.api.auth.service.ManageAuthorizer;
import com.greedy.mokkoji.api.club.dto.response.AdminClubResponse;
import com.greedy.mokkoji.api.club.dto.response.AdminClubsResponse;
import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.admin.entity.Admin;
import com.greedy.mokkoji.db.admin.repository.AdminRepository;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.enums.admin.AdminRole;
import com.greedy.mokkoji.enums.auth.AuthRole;
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
public class AdminClubService {

    private final ClubRepository clubRepository;
    private final AdminRepository adminRepository;
    private final ManageAuthorizer manageAuthorizer;

    @Transactional(readOnly = true)
    public AdminClubsResponse getAdminClubs(
            final AuthRole authRole,
            final Long adminId,
            final UniversityCode universityCode,
            final Pageable pageable
    ) {
        manageAuthorizer.validateAdminAuth(authRole, adminId);

        final Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        final UniversityCode targetUniversityCode = resolveUniversityCode(admin, universityCode);

        final Page<Club> page = clubRepository.findClubsForAdmin(targetUniversityCode, pageable);

        final List<AdminClubResponse> clubs = page.getContent()
                .stream()
                .map(AdminClubResponse::from)
                .toList();

        return AdminClubsResponse.of(
                clubs,
                PageResponse.of(page.getNumber() + 1, page.getSize(), page.getTotalPages(), (int) page.getTotalElements())
        );
    }

    private UniversityCode resolveUniversityCode(final Admin admin, final UniversityCode universityCode) {
        if (admin.getRole() == AdminRole.UNIVERSITY_ADMIN) {
            return admin.getUniversity().getCode();
        }
        return universityCode;
    }
}
