package com.greedy.mokkoji.api.university.service;

import com.greedy.mokkoji.api.university.dto.response.UniversitiesResponse;
import com.greedy.mokkoji.api.university.dto.response.UniversityResponse;
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
    public UniversitiesResponse getUniversities() {
        final List<UniversityResponse> universities = universityRepository.findAll().stream()
                .map(UniversityResponse::from)
                .toList();

        return UniversitiesResponse.of(universities);
    }
}
