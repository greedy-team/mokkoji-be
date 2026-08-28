package com.greedy.mokkoji.db.recruitment.repository;

import com.greedy.mokkoji.db.recruitment.entity.Recruitment;

import java.util.List;

public interface RecruitmentRepositoryCustom {

    public List<Recruitment> findLatestRecruitmentsByClubIds(List<Long> clubIds);
}
