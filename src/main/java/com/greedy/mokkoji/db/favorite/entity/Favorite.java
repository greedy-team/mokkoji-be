package com.greedy.mokkoji.db.favorite.entity;

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
@Table(name = "favorite")
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @JoinColumn(name = "club_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Club club;

    @JoinColumn(name = "user_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Builder
    public Favorite(final Club club, final User user) {
        this.club = club;
        this.user = user;
    }
}
