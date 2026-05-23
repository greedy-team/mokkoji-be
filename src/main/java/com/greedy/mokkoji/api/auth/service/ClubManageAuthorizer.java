package com.greedy.mokkoji.api.auth.service;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.admin.entity.Admin;
import com.greedy.mokkoji.db.admin.repository.AdminRepository;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClubManageAuthorizer {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public void validateCanManageClub(final AuthRole authRole, final Long userId, final Club club) {
        if (userId == null) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }

        if (AuthRole.ADMIN.equals(authRole)) {
            validateMokkojiAdmin(userId);
            return;
        }

        if (AuthRole.USER.equals(authRole)) {
            validateClubMaster(userId, club);
            return;
        }

        throw new MokkojiException(FailMessage.FORBIDDEN_MANAGE_CLUB);
    }

    private void validateMokkojiAdmin(final Long adminId) {
        final Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new MokkojiException(FailMessage.FORBIDDEN_MANAGE_CLUB));

        if (!admin.canManageAnyClub()) {
            throw new MokkojiException(FailMessage.FORBIDDEN_MANAGE_CLUB);
        }
    }

    private void validateClubMaster(final Long userId, final Club club) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        if (!user.canManageClub(club)) {
            throw new MokkojiException(FailMessage.FORBIDDEN_MANAGE_CLUB);
        }
    }
}
