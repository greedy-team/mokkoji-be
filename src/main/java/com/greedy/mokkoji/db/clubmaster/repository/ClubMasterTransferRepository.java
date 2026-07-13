package com.greedy.mokkoji.db.clubmaster.repository;

import com.greedy.mokkoji.db.clubmaster.entity.ClubMasterTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ClubMasterTransferRepository extends JpaRepository<ClubMasterTransfer, Long> {

    void deleteByPreviousMasterId(final Long previousMasterId);

    @Modifying
    @Query("DELETE FROM ClubMasterTransfer cmt WHERE cmt.club.id = :clubId")
    void deleteByClubId(final Long clubId);
}
