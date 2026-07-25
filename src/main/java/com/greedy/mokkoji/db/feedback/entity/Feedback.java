package com.greedy.mokkoji.db.feedback.entity;

import com.greedy.mokkoji.db.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feedback")
public class Feedback extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @Column(name = "rating", columnDefinition = "int", nullable = false)
    private int rating;

    @Column(name = "content", columnDefinition = "text", nullable = false)
    private String content;

    @Builder
    public Feedback(int rating, String content) {
        this.rating = rating;
        this.content = content;
    }
}
