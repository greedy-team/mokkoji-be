package com.greedy.mokkoji.api.user.dto.resopnse;

import com.greedy.mokkoji.db.user.entity.User;
import lombok.Builder;

@Builder
public record UserInformationResponse(User user) {
    public static UserInformationResponse of(User user) {
        return UserInformationResponse.builder()
                .user(user)
                .build();
    }
}
