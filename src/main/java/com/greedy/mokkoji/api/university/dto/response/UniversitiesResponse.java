package com.greedy.mokkoji.api.university.dto.response;

import java.util.List;

public record UniversitiesResponse(
        List<UniversityResponse> universities
) {
    public static UniversitiesResponse of(final List<UniversityResponse> universities) {
        return new UniversitiesResponse(universities);
    }
}
