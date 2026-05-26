package com.greedy.mokkoji.db.user.entity;

import com.greedy.mokkoji.db.BaseTime;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.enums.user.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "`user`")
public class User extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @JoinColumn(name = "university_id", columnDefinition = "bigint")
    @ManyToOne(fetch = FetchType.LAZY)
    private University university;

    @Column(name = "kakao_id", columnDefinition = "varchar(50)", nullable = false, unique = true)
    private String kakaoId;

    @Column(name = "name", columnDefinition = "varchar(50)")
    private String name;

    @Column(name = "email", columnDefinition = "varchar(50)")
    private String email;

    @Column(name = "is_email_on", columnDefinition = "BOOLEAN", nullable = false)
    private boolean isEmailOn;

    @Column(name = "role", columnDefinition = "varchar(50)", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Builder
    public User(University university, String kakaoId, String name, String email, boolean isEmailOn, UserRole role) {
        this.university = university;
        this.kakaoId = kakaoId;
        this.name = name;
        this.email = email;
        this.isEmailOn = isEmailOn;
        this.role = role;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateUniversity(University university) {
        this.university = university;
    }

    public void updateRole(UserRole newRole) {
        this.role = newRole;
    }

    public void updateEmailOn(boolean isEmailOn) {
        this.isEmailOn = isEmailOn;
    }

    public boolean canManageClub(Club club) {
        return this.role == UserRole.CLUB_MASTER && club.getMaster().id.equals(this.id);
    }
}
