package com.greedy.mokkoji.enums.user;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.user.entity.User;

public enum UserRole {
    CLUB_ADMIN,
    GREEDY_ADMIN,
    NORMAL,
    CLUB_MASTER;

    public boolean canRegisterClub() {
        return this == CLUB_ADMIN || this == GREEDY_ADMIN;
    }

    public boolean canManageClub(User user, Club club) {
        return switch (this) {
            case CLUB_ADMIN -> true;
            case CLUB_MASTER -> user.getStudentId().equals(club.getClubMasterStudentId());
            default -> false;
        };
    }
}
