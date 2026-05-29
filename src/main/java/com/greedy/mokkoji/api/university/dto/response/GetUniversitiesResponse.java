package com.greedy.mokkoji.api.university.dto.response;

import java.util.List;

public record GetUniversitiesResponse(
        List<GetUniversityResponse> universities
) {
    public static GetUniversitiesResponse of(final List<GetUniversityResponse> universities) {
        return new GetUniversitiesResponse(universities);
    }
}
