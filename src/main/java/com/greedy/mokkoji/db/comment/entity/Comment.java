package com.greedy.mokkoji.db.comment.entity;

import com.greedy.mokkoji.db.BaseTime;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comment")
public class Comment extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @Column(name = "rate", columnDefinition = "double", nullable = false)
    private Integer rate;

    @Column(name = "content", columnDefinition = "text", nullable = false)
    private String content;

    @JoinColumn(name = "club_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Club club;

    @JoinColumn(name = "user_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Builder
    public Comment(final Integer rate, final String content, final Club club, final User user) {
        this.rate = rate;
        this.content = content;
        this.club = club;
        this.user = user;
    }

    public void updateComment(final Integer rate, final String content) {
        this.rate = rate;
        this.content = content;
    }
}
