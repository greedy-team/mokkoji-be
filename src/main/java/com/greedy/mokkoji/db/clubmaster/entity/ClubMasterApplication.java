package com.greedy.mokkoji.db.clubmaster.entity;

import com.greedy.mokkoji.db.BaseTime;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "club_master_application")
public class ClubMasterApplication extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @JoinColumn(name = "university_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private University university;

    @JoinColumn(name = "club_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Club club;

    @JoinColumn(name = "user_id", columnDefinition = "bigint")
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "user_name", columnDefinition = "varchar(50)", nullable = false)
    private String userName;

    @Column(name = "status", columnDefinition = "varchar(20)", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ApplicationStatus status;

    @Column(name = "reject_reason", columnDefinition = "text")
    private String rejectReason;

    @Builder
    public ClubMasterApplication(
            final University university,
            final Club club,
            final User user,
            final String userName
    ) {
        this.university = university;
        this.club = club;
        this.user = user;
        this.userName = userName;
        this.status = ApplicationStatus.PENDING;
    }

    public void approve(User user) {
        this.club.updateMaster(user);
        this.status = ApplicationStatus.APPROVED;
    }

    public void reject(final String rejectReason) {
        this.status = ApplicationStatus.REJECTED;
        this.rejectReason = rejectReason;
    }
}
