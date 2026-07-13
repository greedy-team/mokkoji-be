package com.greedy.mokkoji.api.auth.service;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.admin.entity.Admin;
import com.greedy.mokkoji.db.admin.repository.AdminRepository;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.admin.AdminRole;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManageAuthorizer {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public void validateCanManageClub(final AuthRole authRole, final Long accountId, final Club club) {
        validateAuthenticated(accountId);

        if (AuthRole.ADMIN.equals(authRole)) {
            Admin admin = findAdminOrThrow(accountId);
            if (AdminRole.MOKKOJI_ADMIN.equals(admin.getRole())) return;
        }

        if (AuthRole.USER.equals(authRole)) {
            User user = findUserOrThrow(accountId);
            if (user.getRole() == UserRole.CLUB_MASTER && club.getMasterId().equals(accountId)) return;
        }

        throw new MokkojiException(FailMessage.FORBIDDEN_MANAGE_CLUB);
    }

    public void validateAdminAuth(final AuthRole authRole, final Long accountId) {
        validateAuthenticated(accountId);

        if (AuthRole.ADMIN.equals(authRole)) return;

        throw new MokkojiException(FailMessage.FORBIDDEN_ADMIN);
    }

    public void validateCanManageUniversity(final Long adminId, final University university) {
        Admin admin = findAdminOrThrow(adminId);

        if (AdminRole.MOKKOJI_ADMIN.equals(admin.getRole())) return;

        if (AdminRole.UNIVERSITY_ADMIN.equals(admin.getRole())
                && university != null
                && university.getId().equals(admin.getUniversityId())) return;

        throw new MokkojiException(FailMessage.FORBIDDEN_MANAGE_CLUB);
    }

    private void validateAuthenticated(final Long accountId) {
        if (accountId == null) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }
    }

    private Admin findAdminOrThrow(final Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_ADMIN));
    }

    private User findUserOrThrow(final Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
    }
}
