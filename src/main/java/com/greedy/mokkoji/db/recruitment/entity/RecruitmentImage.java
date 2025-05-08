package com.greedy.mokkoji.db.recruitment.entity;

import com.greedy.mokkoji.db.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recruitment_image")
public class RecruitmentImage extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @JoinColumn(name = "recruitment_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Recruitment recruitment;

    @Column(name = "image", columnDefinition = "text")
    private String image;

    @Builder
    public RecruitmentImage(final Recruitment recruitment, final String image) {
        this.recruitment = recruitment;
        this.image = image;
    }
}
