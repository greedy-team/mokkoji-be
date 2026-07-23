package com.greedy.mokkoji.api.university.service;

import com.greedy.mokkoji.api.university.dto.response.GetUniversitiesResponse;
import com.greedy.mokkoji.api.university.dto.response.GetUniversityResponse;
import com.greedy.mokkoji.db.university.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniversityService {

    private final UniversityRepository universityRepository;

    @Transactional(readOnly = true)
    public GetUniversitiesResponse getUniversities() {
        final List<GetUniversityResponse> universities = universityRepository.findAll().stream()
                .map(GetUniversityResponse::from)
                .toList();

        return GetUniversitiesResponse.of(universities);
    }
}
