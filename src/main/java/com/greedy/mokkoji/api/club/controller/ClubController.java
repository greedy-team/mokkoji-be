package com.greedy.mokkoji.api.club.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.club.dto.club.request.ClubCreateRequest;
import com.greedy.mokkoji.api.club.dto.club.request.ClubSearchCond;
import com.greedy.mokkoji.api.club.dto.club.request.ClubUpdateRequest;
import com.greedy.mokkoji.api.club.dto.club.response.ClubDetailResponse;
import com.greedy.mokkoji.api.club.dto.club.response.ClubManageDetailResponse;
import com.greedy.mokkoji.api.club.dto.club.response.ClubSearchResponse;
import com.greedy.mokkoji.api.club.dto.club.response.ClubUpdateResponse;
import com.greedy.mokkoji.api.club.service.ClubService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/clubs")
public class ClubController {
    private final ClubService clubService;

    @GetMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<ClubDetailResponse>> getClub(
            @Authentication final AuthCredential authCredential,
            @PathVariable(name = "clubId") final Long clubId) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                clubService.findClub(authCredential.userId(), clubId));
    }

    @GetMapping
    public ResponseEntity<APISuccessResponse<ClubSearchResponse>> getClubs(
            @Authentication final AuthCredential authCredential,
            @ModelAttribute(value = "clubSearchCond") final ClubSearchCond clubSearchCond,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size
    ) {
        final Pageable pageable = PageRequest.of(page - 1, size);

        return APISuccessResponse.of(
                HttpStatus.OK,
                clubService.findClubsByConditions(
                        authCredential.userId(),
                        clubSearchCond.keyword(),
                        clubSearchCond.category(),
                        clubSearchCond.affiliation(),
                        clubSearchCond.recruitStatus(),
                        pageable));
    }

    @PostMapping
    public ResponseEntity<APISuccessResponse<Void>> createClub(
            @RequestBody final ClubCreateRequest clubCreateRequest,
            @Authentication final AuthCredential authCredential
    ) {
        clubService.createClub(authCredential.userId(), clubCreateRequest.name(), clubCreateRequest.category(),
                clubCreateRequest.affiliation(), clubCreateRequest.clubMasterStudentId(),
                clubCreateRequest.image(), clubCreateRequest.instagram(), clubCreateRequest.description());
        return APISuccessResponse.of(HttpStatus.CREATED, null);
    }

    @GetMapping("/manage/{clubId}")
    public ResponseEntity<APISuccessResponse<ClubManageDetailResponse>> getClubManageDetail(
            @PathVariable(name = "clubId") final Long clubId,
            @Authentication final AuthCredential authCredential
    ) {
        return APISuccessResponse.of(HttpStatus.OK, clubService.getClubManageDetail(authCredential.userId(), clubId));
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
                        clubUpdateRequest.logo(),
                        clubUpdateRequest.instagram())
        );
    }
}
