package com.greedy.mokkoji.api.user.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "학생 정보 응답")
public record StudentInformationResponse(
        @Schema(description = "이름", example = "홍길동") String name,
        @Schema(description = "학과", example = "컴퓨터공학과") String department,
        @Schema(description = "학년", example = "4") String grade
) {

    public static StudentInformationResponse of(final String name, final String department, final String grade) {
        return StudentInformationResponse.builder()
                .name(name)
                .department(department)
                .grade(grade)
                .build();
    }
}
