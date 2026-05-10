package com.greedy.mokkoji.db.university.entity;

import com.greedy.mokkoji.db.BaseTime;
import com.greedy.mokkoji.enums.university.UniversityCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "university")
public class University extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @Column(name = "name", columnDefinition = "varchar(100)", nullable = false)
    private String name;

    @Column(name = "code", columnDefinition = "varchar(50)", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private UniversityCode code;

    @Column(name = "logo", columnDefinition = "text")
    private String logo;

    @Builder
    public University(String name, UniversityCode code, String logo) {
        this.name = name;
        this.code = code;
        this.logo = logo;
    }
}
