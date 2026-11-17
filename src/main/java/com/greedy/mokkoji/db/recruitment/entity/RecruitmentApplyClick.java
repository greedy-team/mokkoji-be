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
@Table(name = "recruitment_apply_click")
public class RecruitmentApplyClick extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @JoinColumn(name = "recruitment_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Recruitment recruitment;

    @Builder
    public RecruitmentApplyClick(final Recruitment recruitment) {
        this.recruitment = recruitment;
    }
}
