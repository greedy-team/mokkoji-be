package com.greedy.mokkoji.api.email.service;

import com.greedy.mokkoji.enums.university.UniversityCode;

import java.time.LocalDateTime;
import java.util.List;

public record RecruitmentMailPayload(
        Long clubId,
        String clubName,
        UniversityCode universityCode,
        List<String> receiverMails,
        LocalDateTime recruitStart,
        LocalDateTime recruitEnd
) {}
