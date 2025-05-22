package com.greedy.mokkoji.db.recruitment.repository;

import com.greedy.mokkoji.db.recruitment.entity.RecruitmentImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruitmentImageRepository extends JpaRepository<RecruitmentImage, Long> {
    List<RecruitmentImage> findByRecruitmentIdOrderByIdAsc(Long recruitmentId);

    List<RecruitmentImage> findByRecruitmentId(Long recruitmentId);
}
