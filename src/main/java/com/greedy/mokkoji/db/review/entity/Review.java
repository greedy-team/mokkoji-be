package com.greedy.mokkoji.db.review.entity;

import com.greedy.mokkoji.db.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review")
public class Review extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @Column(name = "user_id", columnDefinition = "bigint", nullable = false)
    private Long userId;

    @Column(name = "rating", columnDefinition = "int", nullable = false)
    private int rating;

    @Column(name = "content", columnDefinition = "text", nullable = false)
    private String content;

    @Builder
    public Review(Long userId, int rating, String content) {
        this.userId = userId;
        this.rating = rating;
        this.content = content;
    }
}
