package com.greedy.mokkoji.db.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

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
    private int grade;

    @Column(name = "email", columnDefinition = "varchar(50)", nullable = false)
    private String email;

    @Builder
    public User(final String studentId, final String name, final String department, final int grade, final String email) {
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        this.grade = grade;
        this.email = email;
    }
}
