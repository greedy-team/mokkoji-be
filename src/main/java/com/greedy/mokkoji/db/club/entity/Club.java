package com.greedy.mokkoji.db.club.entity;

import com.greedy.mokkoji.db.BaseTime;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.user.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "club")
public class Club extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @JoinColumn(name = "university_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private University university;

    @Column(name = "name", columnDefinition = "varchar(50)", nullable = false)
    private String name;

    @Column(name = "club_category", columnDefinition = "varchar(20)", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ClubCategory clubCategory;

    @Column(name = "club_affiliation", columnDefinition = "varchar(20)", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ClubAffiliation clubAffiliation;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "logo", columnDefinition = "text")
    private String logo;

    @Column(name = "instagram", columnDefinition = "text")
    private String instagram;

    @JoinColumn(name = "master_id", columnDefinition = "bigint")
    @OneToOne(fetch = FetchType.LAZY)
    private User master;

    @Builder
    public Club(
            final String name,
            final University university,
            final ClubCategory clubCategory,
            final ClubAffiliation clubAffiliation,
            final String description,
            final String logo,
            final String instagram,
            final User master
    ) {
        this.name = name;
        this.university = university;
        this.clubCategory = clubCategory;
        this.clubAffiliation = clubAffiliation;
        this.description = description;
        this.logo = logo;
        this.instagram = instagram;
        this.master = master;
    }

    public void updateMaster(User nextMaster) {
        if (this.master != null) {
            User previousMaster = this.master;
            previousMaster.updateRole(UserRole.NORMAL);
        }

        this.master = nextMaster;
        nextMaster.updateRole(UserRole.CLUB_MASTER);
    }

    public void updateIfPresent(
            String name,
            ClubCategory category,
            ClubAffiliation affiliation,
            String description,
            String logo,
            String instagram
    ) {
        if (name != null && !name.isBlank()) this.name = name;
        if (category != null) this.clubCategory = category;
        if (affiliation != null) this.clubAffiliation = affiliation;
        if (description != null && !description.isBlank()) this.description = description;
        if (logo != null && !logo.isBlank()) this.logo = logo;
        if (instagram != null && !instagram.isBlank()) this.instagram = instagram;
    }
}
