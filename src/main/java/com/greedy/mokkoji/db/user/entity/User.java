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

    @Column(name = "student_id", columnDefinition = "varchar(20)", nullable = false)
    private String studentId;

    @Column(name = "name", columnDefinition = "varchar(50)", nullable = false)
    private String name;

    @Column(name = "department", columnDefinition = "varchar(50)", nullable = false)
    private String department;

    @Column(name = "grade", columnDefinition = "int", nullable = false)
    private String grade;

    @Column(name = "email", columnDefinition = "varchar(50)", nullable = true)
    private String email;

    @Column(name = "role", columnDefinition = "varchar(50)")
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Builder
    public User(final String studentId, final String name, final String department, final String grade, final String email, final UserRole role) {
        this.studentId = studentId;
        this.name = name;
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
