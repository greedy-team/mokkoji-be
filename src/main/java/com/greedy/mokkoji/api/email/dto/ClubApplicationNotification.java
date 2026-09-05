package com.greedy.mokkoji.api.email.dto;

import com.greedy.mokkoji.db.clubapplication.entity.ClubApplication;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;

public record ClubApplicationNotification(
        Long applicationId,
        String universityName,
        String clubName,
        String clubCategory,
        String clubAffiliation,
        String applicantName,
        String applicantEmail,
        String instagram,
        String description
) {

    public static ClubApplicationNotification of(
            final ClubApplication clubApplication,
            final University university,
            final User applicant
    ) {
        return new ClubApplicationNotification(
                clubApplication.getId(),
                university.getName(),
                clubApplication.getClubName(),
                clubApplication.getClubCategory().getDescription(),
                clubApplication.getClubAffiliation().getDescription(),
                clubApplication.getApplicantName(),
                applicant.getEmail(),
                clubApplication.getInstagram(),
                clubApplication.getDescription()
        );
    }
}
