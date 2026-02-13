package com.greedy.mokkoji.db.report.entity;

import com.greedy.mokkoji.db.BaseTime;
import com.greedy.mokkoji.enums.report.ReportType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "report")
public class Report extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint", nullable = false)
    private Long id;

    @Column(name = "user_id", columnDefinition = "bigint", nullable = false)
    private Long userId;

    @Column(name = "report_type", columnDefinition = "varchar(20)", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ReportType reportType;

    @Column(name = "content", columnDefinition = "text", nullable = false)
    private String content;

    @Builder
    public Report(Long userId, ReportType reportType, String content) {
        this.userId = userId;
        this.reportType = reportType;
        this.content = content;
    }
}
