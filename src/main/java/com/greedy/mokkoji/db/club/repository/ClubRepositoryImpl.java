package com.greedy.mokkoji.db.club.repository;

import com.greedy.mokkoji.api.club.dto.club.ClubSearchCond;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.enums.ClubAffiliation;
import com.greedy.mokkoji.enums.ClubCategory;
import com.greedy.mokkoji.enums.RecruitStatus;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.greedy.mokkoji.db.club.entity.QClub.*;
import static com.greedy.mokkoji.db.recruitment.entity.QRecruitment.*;
import static com.greedy.mokkoji.enums.RecruitStatus.OPEN;

@Repository
@RequiredArgsConstructor
public class ClubRepositoryImpl implements ClubRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Club> findClubs(final ClubSearchCond cond, final Pageable pageable) {

        List<Club> clubs = queryFactory.selectFrom(club)
                .leftJoin(recruitment).on(club.eq(recruitment.club))
                .where(
                        likeClubName(cond.keyword()),
                        equalCategory(cond.category()),
                        equalAffiliation(cond.affiliation()),
                        filterByRecruitStatus(cond.recruitStatus())
                )
                .orderBy(getRecruitmentDuration().asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = getTotalCount(cond);

        return new PageImpl<>(clubs, pageable, total);
    }

    private BooleanExpression likeClubName(final String keyword) {
        return StringUtils.hasText(keyword) ? club.name.like("%" + keyword + "%") : null;
    }

    private BooleanExpression equalCategory(final ClubCategory category) {
        return category != null ? club.clubCategory.eq(category) : null;
    }

    private BooleanExpression equalAffiliation(final ClubAffiliation affiliation) {
        return affiliation != null ? club.clubAffiliation.eq(affiliation) : null;
    }

    private BooleanExpression filterByRecruitStatus(final RecruitStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return status == OPEN ? recruitment.recruitStart.loe(now).and(recruitment.recruitEnd.gt(now)) : null;
    }

    private static NumberTemplate<Long> getRecruitmentDuration() {
        return Expressions.numberTemplate(Long.class, "{0} - {1}", recruitment.recruitEnd, recruitment.recruitStart);
    }

    private Long getTotalCount(final ClubSearchCond cond) {
        return Optional.ofNullable(
                queryFactory.select(club.count())
                        .from(club)
                        .leftJoin(recruitment).on(club.eq(recruitment.club))
                        .where(
                                likeClubName(cond.keyword()),
                                equalCategory(cond.category()),
                                equalAffiliation(cond.affiliation()),
                                filterByRecruitStatus(cond.recruitStatus())
                        )
                        .fetchOne()
        ).orElse(0L);
    }
}
