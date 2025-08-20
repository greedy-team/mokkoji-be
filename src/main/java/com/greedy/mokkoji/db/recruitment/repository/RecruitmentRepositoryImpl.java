package com.greedy.mokkoji.db.recruitment.repository;

import static com.greedy.mokkoji.db.club.entity.QClub.club;
import static com.greedy.mokkoji.db.recruitment.entity.QRecruitment.recruitment;

import com.greedy.mokkoji.db.recruitment.entity.QRecruitment;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecruitmentRepositoryImpl implements RecruitmentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Recruitment> findRecruitments(ClubAffiliation affiliation, ClubCategory category, Pageable pageable) {

        QRecruitment subRecruitment = new QRecruitment("subRecruitment");

        List<Recruitment> recruitments = queryFactory.selectFrom(recruitment)
                .join(recruitment.club, club).fetchJoin()
                .where(
                        equalAffiliation(affiliation),
                        equalCategory(category),
                        recruitment.updatedAt.eq(
                                JPAExpressions
                                        .select(subRecruitment.updatedAt.max())
                                        .from(subRecruitment)
                                        .where(subRecruitment.club.id.eq(recruitment.club.id))
                        )
                )
                .orderBy(recruitment.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(recruitment.count())
                .from(recruitment)
                .join(recruitment.club, club)
                .where(
                        equalAffiliation(affiliation),
                        recruitment.updatedAt.eq(
                                JPAExpressions
                                        .select(subRecruitment.updatedAt.max())
                                        .from(subRecruitment)
                                        .where(subRecruitment.club.id.eq(recruitment.club.id))
                        )
                );

        long total = Optional.ofNullable(countQuery.fetchOne()).orElse(0L);

        return new PageImpl<>(recruitments, pageable, total);
    }

    private BooleanExpression equalAffiliation(ClubAffiliation affiliation) {
        return affiliation != null ? club.clubAffiliation.eq(affiliation) : null;
    }

    private BooleanExpression equalCategory(ClubCategory category) {
        return category != null ? club.clubCategory.eq(category) : null;
    }
}
