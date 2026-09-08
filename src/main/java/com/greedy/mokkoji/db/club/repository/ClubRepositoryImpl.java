package com.greedy.mokkoji.db.club.repository;

import com.greedy.mokkoji.api.club.dto.response.allClubs.ClubWithLatestRecruitment;
import com.greedy.mokkoji.api.club.dto.response.allClubs.LatestRecruitmentInfo;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.recruitment.entity.QRecruitment;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import com.greedy.mokkoji.enums.university.UniversityCode;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
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

import static com.greedy.mokkoji.db.club.entity.QClub.club;
import static com.greedy.mokkoji.db.recruitment.entity.QRecruitment.recruitment;
import static com.greedy.mokkoji.enums.recruitment.RecruitStatus.OPEN;

@Repository
@RequiredArgsConstructor
public class ClubRepositoryImpl implements ClubRepositoryCustom {

    private static final QRecruitment searchTarget = new QRecruitment("searchTarget");

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Club> findClubsForAdmin(final UniversityCode universityCode, final Pageable pageable) {
        final List<Club> clubs = queryFactory.selectFrom(club)
                .leftJoin(club.master).fetchJoin()
                .leftJoin(club.university).fetchJoin()
                .where(equalUniversityCode(universityCode))
                .orderBy(club.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = Optional.ofNullable(
                queryFactory.select(club.count())
                        .from(club)
                        .where(equalUniversityCode(universityCode))
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(clubs, pageable, total);
    }

    @Override
    public Page<Club> findClubsWithLatestRecruitment(
            final UniversityCode universityCode,
            final String keyword,
            final ClubCategory category,
            final ClubAffiliation affiliation,
            final RecruitStatus status,
            final Pageable pageable) {

        final LocalDateTime now = LocalDateTime.now();

        final BooleanExpression matchesKeyword = matchesKeyword(keyword);
        final BooleanExpression matchesStatus = matchesRecruitStatus(status, now);

        final List<Club> clubs = queryFactory.selectFrom(club)
                .leftJoin(recruitment).on(club.eq(recruitment.club), isLatestRecruitment())
                .leftJoin(club.university).fetchJoin()
                .where(
                        matchesKeyword,
                        matchesStatus,
                        equalCategory(category),
                        equalAffiliation(affiliation),
                        equalUniversityCode(universityCode)
                )
                .orderBy(
                        // TODO:: 정렬 방식: 페이지 마다 다른 시간을 넘겨주는 문제(원자성을 잃을 수 있기에) 이야기 해봐야됨
                        getRecruitmentPriority(now).asc(),
                        getRecruitmentDuration(now).asc(),
                        club.id.asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = Optional.ofNullable(
                queryFactory.select(club.count())
                        .from(club)
                        .where(
                                matchesKeyword,
                                matchesStatus,
                                equalCategory(category),
                                equalAffiliation(affiliation),
                                equalUniversityCode(universityCode)
                        )
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(clubs, pageable, total);
    }

    @Override
    public List<ClubWithLatestRecruitment> findAllClubsWithLatestRecruitment(
            final UniversityCode universityCode,
            final String keyword,
            final ClubAffiliation affiliation,
            final ClubCategory category) {
        return queryFactory
                .select(
                        Projections.constructor(
                                ClubWithLatestRecruitment.class,
                                club.id,
                                club.name,
                                club.university.name,
                                club.description,
                                club.logo,
                                Projections.constructor(
                                        LatestRecruitmentInfo.class,
                                        recruitment.id,
                                        recruitment.recruitStart,
                                        recruitment.recruitEnd,
                                        recruitment.isAlwaysRecruiting
                                )
                        )
                )
                .from(club)
                .leftJoin(recruitment).on(
                        recruitment.club.eq(club),
                        isLatestRecruitment()
                )
                .where(
                        likeClubName(keyword),
                        equalUniversityCode(universityCode),
                        equalAffiliation(affiliation),
                        equalCategory(category)
                )
                .fetch();
    }

    private BooleanExpression likeClubName(final String keyword) {
        if (StringUtils.hasText(keyword)) {
            return club.name.like(contains(keyword))
                    .or(club.description.like(contains(keyword)))
                    .or(recruitment.content.like(contains(keyword)));
        }
        return null;
    }

    private BooleanExpression equalUniversityCode(final UniversityCode universityCode) {
        if (universityCode != null) {
            return club.university.code.eq(universityCode);
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

    private BooleanExpression matchesKeyword(final String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        final BooleanExpression matchesClub = club.name.like(contains(keyword))
                .or(club.description.like(contains(keyword)));
        final BooleanExpression matchesRecruitmentContent =
                hasAnyRecruitmentMatching(searchTarget.content.like(contains(keyword)));

        return matchesClub.or(matchesRecruitmentContent);
    }

    private BooleanExpression matchesRecruitStatus(final RecruitStatus status, final LocalDateTime now) {
        if (status != OPEN) {
            return null;
        }

        return hasAnyRecruitmentMatching(
                searchTarget.isAlwaysRecruiting.isTrue()
                        .or(searchTarget.recruitStart.loe(now).and(searchTarget.recruitEnd.gt(now)))
        );
    }

    private BooleanExpression hasAnyRecruitmentMatching(final BooleanExpression condition) {
        return JPAExpressions.selectOne()
                .from(searchTarget)
                .where(searchTarget.club.eq(club), condition)
                .exists();
    }

    private BooleanExpression isLatestRecruitment() {
        final QRecruitment latestId = new QRecruitment("latestId");
        final QRecruitment latestCreatedAt = new QRecruitment("latestCreatedAt");

        return recruitment.id.eq(
                JPAExpressions.select(latestId.id.max())
                        .from(latestId)
                        .where(
                                latestId.club.eq(club),
                                latestId.createdAt.eq(
                                        JPAExpressions.select(latestCreatedAt.createdAt.max())
                                                .from(latestCreatedAt)
                                                .where(latestCreatedAt.club.eq(club))
                                )
                        )
        );
    }

    private NumberExpression<Integer> getRecruitmentPriority(final LocalDateTime now) {
        return new CaseBuilder()
                .when(recruitment.recruitStart.loe(now).and(recruitment.recruitEnd.gt(now)))
                .then(0)
                .otherwise(1);
    }

    private NumberTemplate<Long> getRecruitmentDuration(final LocalDateTime now) {
        return Expressions.numberTemplate(Long.class, "TIMESTAMPDIFF(MINUTE, {0}, {1})", recruitment.recruitEnd, now);
    }

    private String contains(final String keyword) {
        return "%" + keyword + "%";
    }
}
