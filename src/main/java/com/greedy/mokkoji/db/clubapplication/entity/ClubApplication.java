package com.greedy.mokkoji.db.clubapplication.entity;

import com.greedy.mokkoji.db.BaseTime;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "club_application")
public class ClubApplication extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @JoinColumn(name = "university_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private University university;

    @JoinColumn(name = "applicant_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User applicant;

    @Column(name = "application_name", columnDefinition = "varchar(50)", nullable = false)
    private String applicantName;

    @Column(name = "name", columnDefinition = "varchar(50)", nullable = false)
    private String clubName;

    @Column(name = "status", columnDefinition = "varchar(20)", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column(name = "reject_reason", columnDefinition = "text")
    private String rejectReason;

    @Column(name = "club_category", columnDefinition = "varchar(20)", nullable = false)
    @Enumerated(EnumType.STRING)
    private ClubCategory clubCategory;

    @Column(name = "club_affiliation", columnDefinition = "varchar(20)", nullable = false)
    @Enumerated(EnumType.STRING)
    private ClubAffiliation clubAffiliation;

    @Column(name = "logo", columnDefinition = "text")
    private String logo;

    @Column(name = "instagram", columnDefinition = "text")
    private String instagram;

    @Column(name = "description", columnDefinition = "text", nullable = false)
    private String description;

    @Builder
    public ClubApplication(
            University university,
            User applicant,
            String applicantName,
            String clubName,
            ClubCategory clubCategory,
            ClubAffiliation clubAffiliation,
            String logo,
            String instagram,
            String description)
    {
        this.university = university;
        this.applicant = applicant;
        this.applicantName = applicantName;
        this.clubName = clubName;
        this.status = ApplicationStatus.PENDING;
        this.rejectReason = null;
        this.clubCategory = clubCategory;
        this.clubAffiliation = clubAffiliation;
        this.logo = logo;
        this.instagram = instagram;
        this.description = description;
    }

    public void approve() {
        this.status = ApplicationStatus.APPROVED;
    }

    public void reject(String rejectReason) {
        this.rejectReason = rejectReason;
        this.status = ApplicationStatus.REJECTED;
    }
}
