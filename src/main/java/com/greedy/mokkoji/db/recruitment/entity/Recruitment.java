package com.greedy.mokkoji.db.recruitment.entity;

import com.greedy.mokkoji.db.BaseTime;
import com.greedy.mokkoji.db.club.entity.Club;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recruitment")
public class Recruitment extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @JoinColumn(name = "club_id", columnDefinition = "bigint", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private Club club;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "recruit_start", columnDefinition = "timestamp")
    private LocalDateTime recruitStart;

    @Column(name = "recruit_end", columnDefinition = "timestamp")
    private LocalDateTime recruitEnd;

    @Builder
    public Recruitment(Club club, String content, LocalDateTime recruitStart, LocalDateTime recruitEnd) {
        this.club = club;
        this.content = content;
        this.recruitStart = recruitStart;
        this.recruitEnd = recruitEnd;
    }
}
