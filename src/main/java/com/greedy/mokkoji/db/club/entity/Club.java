package com.greedy.mokkoji.db.club.entity;

import com.greedy.mokkoji.db.BaseTime;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
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

    @Column(name = "club_master_student_id", columnDefinition = "varchar(20)")
    private String clubMasterStudentId;

    @Builder
    public Club(final String name, final ClubCategory clubCategory, final ClubAffiliation clubAffiliation, final String description, final String logo, final String instagram, final String clubMasterStudentId) {
        this.name = name;
        this.clubCategory = clubCategory;
        this.clubAffiliation = clubAffiliation;
        this.description = description;
        this.logo = logo;
        this.instagram = instagram;
        this.clubMasterStudentId = clubMasterStudentId;
    }
}
