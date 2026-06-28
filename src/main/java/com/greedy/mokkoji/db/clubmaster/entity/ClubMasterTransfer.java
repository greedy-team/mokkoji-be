package com.greedy.mokkoji.db.clubmaster.entity;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.BaseTime;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.clubmaster.TransferStatus;
import com.greedy.mokkoji.enums.message.FailMessage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "club_master_transfer")
public class ClubMasterTransfer extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @JoinColumn(name = "club_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Club club;

    @JoinColumn(name = "previous_master_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User previousMaster;

    @Column(name = "next_master_name", columnDefinition = "varchar(50)", nullable = false)
    private String nextMasterName;

    @Column(name = "next_master_email", columnDefinition = "varchar(50)", nullable = false)
    private String nextMasterEmail;

    @Column(name = "status", columnDefinition = "varchar(20)", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TransferStatus status;

    @Builder
    public ClubMasterTransfer(Club club, User previousMaster, String nextMasterName, String nextMasterEmail) {
        this.club = club;
        this.previousMaster = previousMaster;
        this.nextMasterName = nextMasterName;
        this.nextMasterEmail = nextMasterEmail;
        this.status = TransferStatus.PENDING;
    }

    public void approve() {
        validatePendingStatus();
        this.status = TransferStatus.APPROVED;
    }

    private void validatePendingStatus() {
        if (this.status != TransferStatus.PENDING) {
            throw new MokkojiException(FailMessage.CONFLICT_CLUB_MASTER_TRANSFER_STATUS);
        }
    }
}
