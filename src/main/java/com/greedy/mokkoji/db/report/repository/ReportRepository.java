package com.greedy.mokkoji.db.report.repository;

import com.greedy.mokkoji.db.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
