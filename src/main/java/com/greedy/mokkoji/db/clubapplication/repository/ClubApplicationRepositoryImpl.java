package com.greedy.mokkoji.db.clubapplication.repository;

import com.greedy.mokkoji.db.clubapplication.entity.ClubApplication;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
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

import static com.greedy.mokkoji.db.clubapplication.entity.QClubApplication.clubApplication;

@Repository
@RequiredArgsConstructor
public class ClubApplicationRepositoryImpl implements ClubApplicationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ClubApplication> findByConditions(
            final UniversityCode universityCode,
            final ApplicationStatus status,
            final Pageable pageable
    ) {
        final List<ClubApplication> content = queryFactory
                .selectFrom(clubApplication)
                .where(
                        equalUniversityCode(universityCode),
                        equalStatus(status)
                )
                .orderBy(clubApplication.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = Optional.ofNullable(
                queryFactory
                        .select(clubApplication.count())
                        .from(clubApplication)
                        .where(
                                equalUniversityCode(universityCode),
                                equalStatus(status)
                        )
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression equalUniversityCode(final UniversityCode universityCode) {
        if (universityCode != null) {
            return clubApplication.university.code.eq(universityCode);
        }
        return null;
    }

    private BooleanExpression equalStatus(final ApplicationStatus status) {
        if (status != null) {
            return clubApplication.status.eq(status);
        }
        return null;
    }
}
