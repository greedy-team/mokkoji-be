package com.greedy.mokkoji.db.user.entity;

import com.greedy.mokkoji.enums.user.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @Column(name = "unique_id", columnDefinition = "varchar(20)", nullable = false)
    private String uniqueId;

    @Column(name = "student_id", columnDefinition = "varchar(20)", nullable = true)
    private String studentId;

    @Column(name = "name", columnDefinition = "varchar(50)", nullable = true)
    private String name;

    @Column(name = "nickname", columnDefinition = "varchar(50)", nullable = false)
    private String nickname;

    @Column(name = "department", columnDefinition = "varchar(50)", nullable = true)
    private String department;

    @Column(name = "grade", columnDefinition = "varchar(10)", nullable = true)
    private String grade;

    @Column(name = "email", columnDefinition = "varchar(50)", nullable = true)
    private String email;

    @Column(name = "role", columnDefinition = "varchar(50)", nullable = true)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Builder
    public User(String uniqueId, String studentId, String name, String nickname, String department, String grade, String email, UserRole role) {
        this.uniqueId = uniqueId;
        this.studentId = studentId;
        this.name = name;
        this.nickname = nickname;
        this.department = department;
        this.grade = grade;
        this.email = email;
        this.role = role;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void grantRole(UserRole newRole) {
        this.role = newRole;
    }
}
