package com.greedy.mokkoji.api.user.dto.resopnse;

import com.greedy.mokkoji.db.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "사용자 정보 응답")
public record UserInformationResponse(
        @Schema(description = "사용자 정보") User user) {
    public static UserInformationResponse of(User user) {
        return UserInformationResponse.builder()
                .user(user)
                .build();
    }
}
