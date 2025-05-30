package com.greedy.mokkoji.db.report.entity;

import com.greedy.mokkoji.enums.report.ReportType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "report")
public class Report {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @Column(name = "user_id", columnDefinition = "bigint", nullable = false)
    private Long userId;

    @Column(name = "report_type", columnDefinition = "varchar(20)", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ReportType reportType;

    @Column(name = "content", columnDefinition = "text", nullable = false)
    private String content;

    @CreationTimestamp
    private LocalDateTime created_at;

    @Builder
    public Report(Long userId, ReportType reportType, String content) {
        this.userId = userId;
        this.reportType = reportType;
        this.content = content;
    }
}
