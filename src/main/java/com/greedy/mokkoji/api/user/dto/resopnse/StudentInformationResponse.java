package com.greedy.mokkoji.api.user.dto.resopnse;

import lombok.Builder;

@Builder
public record StudentInformationResponse(
        String name,
        String department,
        String grade
) {

    public static StudentInformationResponse of(final String name, final String department, final String grade) {
        return StudentInformationResponse.builder()
                .name(name)
                .department(department)
                .grade(grade)
                .build();
    }
}
