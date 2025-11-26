package com.greedy.mokkoji.api.club.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "동아리 정보 수정 응답")
public record ClubUpdateResponse(
        @Schema(description = "로고 업로드용 Presigned URL", example = "https://s3.amazonaws.com/bucket/club-logo/1/greedy_logo.jpg") String updateLogo,
        @Schema(description = "기존 로고 삭제용 Presigned URL", example = "https://s3.amazonaws.com/bucket/club-logo/1/old_logo.jpg") String deleteLogo
) {
    public static ClubUpdateResponse of(String updateLogo, String deleteLogo) {
        return new ClubUpdateResponse(updateLogo, deleteLogo);
    }
}
