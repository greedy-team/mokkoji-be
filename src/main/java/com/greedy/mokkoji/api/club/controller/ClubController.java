package com.greedy.mokkoji.api.club.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.club.dto.club.ClubDetailResponse;
import com.greedy.mokkoji.api.club.dto.club.ClubSearchCond;
import com.greedy.mokkoji.api.club.dto.club.ClubSearchResponse;
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

    private static final Long USER_ID = 1L;
    private final ClubService clubService;

    @GetMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<ClubDetailResponse>> getClub(@Authentication AuthCredential authCredential, @PathVariable("clubId") final Long clubId) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                clubService.findClub(authCredential, clubId));
    }

    @GetMapping
    public ResponseEntity<APISuccessResponse<ClubSearchResponse>> getClubs(
            @Authentication AuthCredential authCredential,
            @ModelAttribute(value = "clubSearchCond") final ClubSearchCond clubSearchCond,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size
    ) {
        final Pageable pageable = PageRequest.of(page - 1, size);

        return APISuccessResponse.of(
                HttpStatus.OK,
                clubService.findClubsByConditions(
                        authCredential,
                        clubSearchCond.keyword(),
                        clubSearchCond.category(),
                        clubSearchCond.affiliation(),
                        clubSearchCond.recruitStatus(),
                        pageable));
    }
}
