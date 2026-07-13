package com.greedy.mokkoji.api.auth.controller.argumentResolver;

import com.greedy.mokkoji.enums.auth.AuthRole;

public record AuthCredential(
        AuthRole authRole,
        Long accountId
) {
}
