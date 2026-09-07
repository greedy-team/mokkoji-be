package com.greedy.mokkoji.db.recruitment.repository;

import com.greedy.mokkoji.db.recruitment.entity.QRecruitment;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.greedy.mokkoji.db.club.entity.QClub.club;
import static com.greedy.mokkoji.db.recruitment.entity.QRecruitment.recruitment;

@Repository
@RequiredArgsConstructor
public class RecruitmentRepositoryImpl implements RecruitmentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Recruitment> findLatestRecruitmentsByClubIds(List<Long> favoriteClubIds) {
        QRecruitment subRecruitment = new QRecruitment("subRecruitment");

        return queryFactory.selectFrom(recruitment)
                .join(recruitment.club, club).fetchJoin()
                .where(
                        club.id.in(favoriteClubIds),
                        recruitment.createdAt.eq(
                                JPAExpressions
                                        .select(subRecruitment.createdAt.max())
                                        .from(subRecruitment)
                                        .where(subRecruitment.club.id.eq(recruitment.club.id))
                        )
                )
                .orderBy(recruitment.createdAt.desc())
                .fetch();
    }
}
