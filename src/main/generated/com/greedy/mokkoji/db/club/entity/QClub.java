package com.greedy.mokkoji.db.club.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QClub is a Querydsl query type for Club
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClub extends EntityPathBase<Club> {

    private static final long serialVersionUID = -1370491722L;

    public static final QClub club = new QClub("club");

    public final EnumPath<com.greedy.mokkoji.enums.club.ClubAffiliation> clubAffiliation = createEnum("clubAffiliation", com.greedy.mokkoji.enums.club.ClubAffiliation.class);

    public final EnumPath<com.greedy.mokkoji.enums.club.ClubCategory> clubCategory = createEnum("clubCategory", com.greedy.mokkoji.enums.club.ClubCategory.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath instagram = createString("instagram");

    public final StringPath logo = createString("logo");

    public final StringPath name = createString("name");

    public QClub(String variable) {
        super(Club.class, forVariable(variable));
    }

    public QClub(Path<? extends Club> path) {
        super(path.getType(), path.getMetadata());
    }

    public QClub(PathMetadata metadata) {
        super(Club.class, metadata);
    }

}

