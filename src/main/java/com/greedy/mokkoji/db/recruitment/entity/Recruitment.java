package com.greedy.mokkoji.db.recruitment.entity;

import com.greedy.mokkoji.db.BaseTime;
import com.greedy.mokkoji.db.club.entity.Club;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    @ManyToOne(fetch = FetchType.LAZY)
    private Club club;

    @Column(name = "title", columnDefinition = "text")
    private String title;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "recruit_start", columnDefinition = "timestamp")
    private LocalDateTime recruitStart;

    @Column(name = "recruit_end", columnDefinition = "timestamp")
    private LocalDateTime recruitEnd;

    @Column(name = "recruit_form", columnDefinition = "text")
    private String recruitForm;

    @Builder
    public Recruitment(final Club club, final String title, final String content, final LocalDateTime recruitStart, final LocalDateTime recruitEnd, final String recruitForm) {
        this.club = club;
        this.title = title;
        this.content = content;
        this.recruitStart = recruitStart;
        this.recruitEnd = recruitEnd;
        this.recruitForm = recruitForm;
    }
}
