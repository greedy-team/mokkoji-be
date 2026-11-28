package com.greedy.mokkoji.api.auth.controller.argumentResolver;

import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.user.UserRole;

public record AuthCredential(
        AuthRole authRole,
        Long accountId,
        UserRole userRole
) {
    public AuthCredential(final AuthRole authRole, final Long accountId) {
        this(authRole, accountId, null);
    }
}
