package com.greedy.mokkoji.db.club.repository;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.enums.RecruitStatus;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
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
    public Page<Club> findClubs(final String keyword,
                                final ClubCategory category,
                                final ClubAffiliation affiliation,
                                final RecruitStatus status,
                                final Pageable pageable) {

        final List<Club> clubs = queryFactory.selectFrom(club)
                .leftJoin(recruitment).on(club.eq(recruitment.club))
                .where(
                        likeClubName(keyword),
                        equalCategory(category),
                        equalAffiliation(affiliation),
                        filterByRecruitStatus(status)
                )
                .orderBy(
                        getRecruitmentPriority().asc(),
                        getRecruitmentDuration().asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = Optional.ofNullable(
                queryFactory.select(club.count())
                        .from(club)
                        .leftJoin(recruitment).on(club.eq(recruitment.club))
                        .where(
                                likeClubName(keyword),
                                equalCategory(category),
                                equalAffiliation(affiliation),
                                filterByRecruitStatus(status)
                        )
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(clubs, pageable, total);
    }

    private BooleanExpression likeClubName(final String keyword) {
        if (StringUtils.hasText(keyword)) {
            return club.name.like("%" + keyword + "%");
        }
        return null;
    }

    private BooleanExpression equalCategory(final ClubCategory category) {
        if (category != null) {
            return club.clubCategory.eq(category);
        }
        return null;
    }

    private BooleanExpression equalAffiliation(final ClubAffiliation affiliation) {
        if (affiliation != null) {
            return club.clubAffiliation.eq(affiliation);
        }
        return null;
    }

    private BooleanExpression filterByRecruitStatus(final RecruitStatus status) {
        final LocalDateTime now = LocalDateTime.now();
        if (status == OPEN) {
            return recruitment.recruitStart.loe(now).and(recruitment.recruitEnd.gt(now));
        }
        return null;
    }

    private NumberExpression<Integer> getRecruitmentPriority() {
        return new CaseBuilder()
                .when(recruitment.recruitStart.loe(LocalDateTime.now()).and(recruitment.recruitEnd.gt(LocalDateTime.now())))
                .then(0)
                .otherwise(1);
    }

    private NumberTemplate<Long> getRecruitmentDuration() {
        return Expressions.numberTemplate(Long.class, "TIMESTAMPDIFF(DAY, {0}, {1})", LocalDateTime.now(), recruitment.recruitEnd);
    }
}
