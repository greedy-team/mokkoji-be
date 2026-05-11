package com.greedy.mokkoji.api.club.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.club.dto.request.ClubCreateRequest;
import com.greedy.mokkoji.api.club.dto.request.ClubSearchCond;
import com.greedy.mokkoji.api.club.dto.request.ClubUpdateRequest;
import com.greedy.mokkoji.api.club.dto.response.ClubDetailResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubManageDetailResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubUpdateResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubsPaginationResponse;
import com.greedy.mokkoji.api.club.dto.response.allClubs.AllClubsResponse;
import com.greedy.mokkoji.api.club.service.ClubService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.university.UniversityCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/clubs")
public class ClubController implements ClubControllerSwagger {

    private final ClubService clubService;

    @GetMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<ClubDetailResponse>> getClub(
            @Authentication final AuthCredential authCredential,
            @PathVariable(name = "clubId") final Long clubId
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                clubService.findClub(authCredential.userId(), clubId)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<APISuccessResponse<ClubsPaginationResponse>> getClubs(
            @Authentication final AuthCredential authCredential,
            @ModelAttribute(value = "clubSearchCond") final ClubSearchCond clubSearchCond,
            @RequestParam(value = "universityCode") final UniversityCode universityCode,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size
    ) {
        final Pageable pageable = PageRequest.of(page - 1, size);
        return APISuccessResponse.of(
                HttpStatus.OK,
                clubService.findClubsByConditions(
                        authCredential.userId(),
                        universityCode,
                        clubSearchCond.keyword(),
                        clubSearchCond.category(),
                        clubSearchCond.affiliation(),
                        clubSearchCond.recruitStatus(),
                        pageable
                )
        );
    }

    @GetMapping
    public ResponseEntity<APISuccessResponse<AllClubsResponse>> getAllClubs(
            @Authentication final AuthCredential authCredential,
            @RequestParam(value = "universityCode") final UniversityCode universityCode,
            @RequestParam(value = "keyword", required = false) final String keyword,
            @RequestParam(value = "affiliation", required = false) final ClubAffiliation affiliation,
            @RequestParam(value = "category", required = false) final ClubCategory category,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size
    ) {
        final Pageable pageable = PageRequest.of(page - 1, size);
        return APISuccessResponse.of(
                HttpStatus.OK,
                clubService.getAllClubs(authCredential.userId(), universityCode, keyword, affiliation, category, pageable)
        );
    }


    @PostMapping
    public ResponseEntity<APISuccessResponse<Void>> createClub(
            @RequestBody final ClubCreateRequest clubCreateRequest,
            @Authentication final AuthCredential authCredential
    ) {
        clubService.createClub(
                authCredential.userId(),
                clubCreateRequest.name(),
                clubCreateRequest.category(),
                clubCreateRequest.affiliation(),
                clubCreateRequest.clubMasterStudentId()
        );
        return APISuccessResponse.of(HttpStatus.CREATED, null);
    }

    @GetMapping("/manage/{clubId}")
    public ResponseEntity<APISuccessResponse<ClubManageDetailResponse>> getClubManageDetail(
            @PathVariable(name = "clubId") final Long clubId,
            @Authentication final AuthCredential authCredential
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                clubService.getClubManageDetail(authCredential.userId(), clubId)
        );
    }

    @PatchMapping("/manage/{clubId}")
    public ResponseEntity<APISuccessResponse<ClubUpdateResponse>> updateClub(
            @PathVariable(name = "clubId") final Long clubId,
            @Authentication final AuthCredential authCredential,
            @RequestBody final ClubUpdateRequest clubUpdateRequest
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                clubService.updateClub(
                        authCredential.userId(),
                        clubId,
                        clubUpdateRequest.name(),
                        clubUpdateRequest.category(),
                        clubUpdateRequest.affiliation(),
                        clubUpdateRequest.description(),
                        clubUpdateRequest.clubMasterStudentId(),
                        clubUpdateRequest.logo(),
                        clubUpdateRequest.instagram()
                )
        );
    }
}
