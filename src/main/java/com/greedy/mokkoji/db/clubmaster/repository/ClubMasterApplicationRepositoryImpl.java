package com.greedy.mokkoji.db.clubmaster.repository;

import com.greedy.mokkoji.db.clubmaster.entity.ClubMasterApplication;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.university.UniversityCode;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.greedy.mokkoji.db.clubmaster.entity.QClubMasterApplication.clubMasterApplication;

@Repository
@RequiredArgsConstructor
public class ClubMasterApplicationRepositoryImpl implements ClubMasterApplicationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ClubMasterApplication> findByConditions(
            final UniversityCode universityCode,
            final ApplicationStatus status,
            final ClubAffiliation affiliation,
            final Pageable pageable
    ) {
        final List<ClubMasterApplication> content = queryFactory
                .selectFrom(clubMasterApplication)
                .join(clubMasterApplication.university).fetchJoin()
                .join(clubMasterApplication.club).fetchJoin()
                .where(
                        equalUniversityCode(universityCode),
                        equalStatus(status),
                        equalAffiliation(affiliation)
                )
                .orderBy(clubMasterApplication.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = Optional.ofNullable(
                queryFactory
                        .select(clubMasterApplication.count())
                        .from(clubMasterApplication)
                        .join(clubMasterApplication.club)
                        .where(
                                equalUniversityCode(universityCode),
                                equalStatus(status),
                                equalAffiliation(affiliation)
                        )
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression equalUniversityCode(final UniversityCode universityCode) {
        if (universityCode != null) {
            return clubMasterApplication.university.code.eq(universityCode);
        }
        return null;
    }

    private BooleanExpression equalStatus(final ApplicationStatus status) {
        if (status != null) {
            return clubMasterApplication.status.eq(status);
        }
        return null;
    }

    private BooleanExpression equalAffiliation(final ClubAffiliation affiliation) {
        if (affiliation != null) {
            return clubMasterApplication.club.clubAffiliation.eq(affiliation);
        }
        return null;
    }
}
