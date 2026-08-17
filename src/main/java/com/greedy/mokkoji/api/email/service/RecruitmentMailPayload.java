package com.greedy.mokkoji.api.email.service;

import java.time.LocalDateTime;
import java.util.List;

public record RecruitmentMailPayload(
        Long clubId,
        String clubName,
        List<String> receiverMails,
        LocalDateTime recruitStart,
        LocalDateTime recruitEnd
) {}
