package com.greedy.mokkoji.db.admin.entity;

import com.greedy.mokkoji.db.BaseTime;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.enums.admin.AdminRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "admin")
public class Admin extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @JoinColumn(name = "university_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private University university;

    @Column(name = "login_id", columnDefinition = "varchar(100)", nullable = false)
    private String loginId;

    @Column(name = "password", columnDefinition = "varchar(100)", nullable = false)
    private String password;

    @Column(name = "role", columnDefinition = "varchar(50)", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private AdminRole role;

    @Builder
    public Admin(
            final University university,
            final String loginId,
            final String password,
            final AdminRole role
    ) {
        this.university = university;
        this.loginId = loginId;
        this.password = password;
        this.role = role;
    }
}
