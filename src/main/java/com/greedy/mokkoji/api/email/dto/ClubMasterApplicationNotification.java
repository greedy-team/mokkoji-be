package com.greedy.mokkoji.api.email.dto;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.clubmaster.entity.ClubMasterApplication;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;

public record ClubMasterApplicationNotification(
        Long applicationId,
        String universityName,
        String clubName,
        String applicantName,
        String applicantEmail
) {

    public static ClubMasterApplicationNotification of(
            final ClubMasterApplication application,
            final University university,
            final Club club,
            final User applicant
    ) {
        return new ClubMasterApplicationNotification(
                application.getId(),
                university.getName(),
                club.getName(),
                application.getUserName(),
                applicant.getEmail()
        );
    }
}
