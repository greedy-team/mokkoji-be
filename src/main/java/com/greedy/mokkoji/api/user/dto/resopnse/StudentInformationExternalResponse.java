package com.greedy.mokkoji.api.user.dto.resopnse;

import lombok.Builder;

@Builder
public record StudentInformationExternalResponse(
        String name,
        String department,
        String grade
) {

    public static StudentInformationExternalResponse of(final String name, final String department, final String grade) {
        return StudentInformationExternalResponse.builder()
                .name(name)
                .department(department)
                .grade(grade)
                .build();
    }
}
