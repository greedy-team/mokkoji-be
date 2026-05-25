package com.greedy.mokkoji.db.clubmaster.entity;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.clubmaster.ClubMasterTransferStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "club_master_application")
public class ClubMasterTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @JoinColumn(name = "club_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Club club;

    @JoinColumn(name = "previous_master_id", columnDefinition = "bigint")
    @OneToOne(fetch = FetchType.LAZY)
    private User previousMaster;

    @Column(name = "next_master_name", columnDefinition = "varchar(50)", nullable = false)
    private String nextMasterName;

    @Column(name = "next_master_email", columnDefinition = "varchar(50)")
    private String nextMasterEmail;

    @Column(name = "status", columnDefinition = "varchar(20)", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ClubMasterTransferStatus status;

    @Builder
    public ClubMasterTransfer(Club club, User previousMaster, String nextMasterName, String nextMasterEmail) {
        this.club = club;
        this.previousMaster = previousMaster;
        this.nextMasterName = nextMasterName;
        this.nextMasterEmail = nextMasterEmail;
        this.status = ClubMasterTransferStatus.PENDING;
    }
}
