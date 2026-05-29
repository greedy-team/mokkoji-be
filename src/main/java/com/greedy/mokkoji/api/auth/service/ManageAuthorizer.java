package com.greedy.mokkoji.api.auth.service;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.admin.entity.Admin;
import com.greedy.mokkoji.db.admin.repository.AdminRepository;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManageAuthorizer {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public void validateCanManageClub(final AuthRole authRole, final Long userId, final Club club) {
        if (userId == null) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }

        if (AuthRole.ADMIN.equals(authRole)) {
            Admin admin = findAdminOrThrow(userId);
            admin.canManageAllUniversitiesAndClubs();
        }

        if (AuthRole.USER.equals(authRole)) {
            User user = findUserOrThrow(userId);
            user.canManageClub(club);
        }

        throw new MokkojiException(FailMessage.FORBIDDEN_MANAGE_CLUB);
    }

    public void validateCanManageUniversity(final AuthRole authRole, final Long userId, final University university) {
        if (userId == null) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }

        if (!AuthRole.ADMIN.equals(authRole)) {
            throw new MokkojiException(FailMessage.FORBIDDEN_MANAGE_UNIVERSITY_CLUB);
        }

        Admin admin = findAdminOrThrow(userId);
        admin.canManageUniversity(university);
    }

    private Admin findAdminOrThrow(final Long userId) {
        return adminRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_ADMIN));
    }

    private User findUserOrThrow(final Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
    }
}
