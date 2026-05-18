package com.greedy.mokkoji.api.auth.controller.argumentResolver;

import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.university.UniversityCode;

public record AuthCredential(
        AuthRole authRole,
        Long userId
) {
}
