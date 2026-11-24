package com.greedy.mokkoji.api.notification.dto;

import com.greedy.mokkoji.enums.user.UserRole;

public record ConnectPayload(
        String message,
        UserRole role
) {

    private static final String CONNECTED_MESSAGE = "connected";

    public static ConnectPayload of(final UserRole role) {
        return new ConnectPayload(CONNECTED_MESSAGE, role);
    }
}
