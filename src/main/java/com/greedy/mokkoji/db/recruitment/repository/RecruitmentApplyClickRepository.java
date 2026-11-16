package com.greedy.mokkoji.db.recruitment.repository;

import com.greedy.mokkoji.db.recruitment.entity.RecruitmentApplyClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruitmentApplyClickRepository extends JpaRepository<RecruitmentApplyClick, Long> {

    @Modifying
    @Query("DELETE FROM RecruitmentApplyClick c WHERE c.recruitment.id = :recruitmentId")
    void deleteByRecruitmentId(final Long recruitmentId);

    @Modifying
    @Query("DELETE FROM RecruitmentApplyClick c WHERE c.recruitment.id IN :recruitmentIds")
    void deleteByRecruitmentIdIn(final List<Long> recruitmentIds);
}
