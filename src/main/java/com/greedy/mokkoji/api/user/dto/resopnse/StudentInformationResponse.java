package com.greedy.mokkoji.api.user.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record StudentInformationResponse(
        @Schema(example = "홍길동") String name,
        @Schema(example = "컴퓨터공학과") String department,
        @Schema(example = "4") String grade
) {

    public static StudentInformationResponse of(final String name, final String department, final String grade) {
        return StudentInformationResponse.builder()
                .name(name)
                .department(department)
                .grade(grade)
                .build();
    }
}
