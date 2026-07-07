package com.greedy.mokkoji.db.clubmaster.repository;

import com.greedy.mokkoji.db.clubmaster.entity.ClubMasterTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMasterTransferRepository extends JpaRepository<ClubMasterTransfer, Long> {

    void deleteByPreviousMasterId(final Long previousMasterId);

    void deleteByClubId(final Long clubId);
}
