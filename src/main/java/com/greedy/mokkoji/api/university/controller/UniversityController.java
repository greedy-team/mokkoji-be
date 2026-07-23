package com.greedy.mokkoji.api.university.controller;

import com.greedy.mokkoji.api.university.dto.response.GetUniversitiesResponse;
import com.greedy.mokkoji.api.university.service.UniversityService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/universities")
public class UniversityController implements UniversityControllerSwagger {

    private final UniversityService universityService;

    @GetMapping
    public ResponseEntity<APISuccessResponse<GetUniversitiesResponse>> getUniversities() {
        return APISuccessResponse.of(
                HttpStatus.OK,
                universityService.getUniversities()
        );
    }
}
