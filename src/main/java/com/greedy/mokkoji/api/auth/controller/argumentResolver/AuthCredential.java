package com.greedy.mokkoji.api.auth.controller.argumentResolver;

import com.greedy.mokkoji.enums.admin.AdminRole;
import com.greedy.mokkoji.enums.auth.AuthRole;

public record AuthCredential(
        AuthRole authRole,
        AdminRole adminRole,
        Long accountId
) {
}
