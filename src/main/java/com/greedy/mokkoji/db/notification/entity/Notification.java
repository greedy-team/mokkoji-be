package com.greedy.mokkoji.db.notification.entity;

import com.greedy.mokkoji.db.BaseTime;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.notification.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification")
public class Notification extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @JoinColumn(name = "user_id", columnDefinition = "bigint", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private NotificationType type;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "target_id", columnDefinition = "bigint")
    private Long targetId;

    @Column(name = "is_read", columnDefinition = "BOOLEAN", nullable = false)
    private boolean isRead;

    @Builder
    public Notification(
            final User user,
            final NotificationType type,
            final String title,
            final String content,
            final Long targetId
    ) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.content = content;
        this.targetId = targetId;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
