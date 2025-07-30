package com.greedy.mokkoji.api.report.service;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.report.entity.Report;
import com.greedy.mokkoji.db.report.repository.ReportRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.report.ReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Transactional
    public Void createReport(Long userId, ReportType reportType, String content) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        Report report = Report.builder()
                .userId(userId)
                .reportType(reportType)
                .content(content)
                .build();

        reportRepository.save(report);
        return null;
    }
}
