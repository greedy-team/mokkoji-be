package com.greedy.mokkoji.api.favorite.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.club.dto.response.ClubsPaginationResponse;
import com.greedy.mokkoji.api.favorite.dto.request.RecruitClubsRequest;
import com.greedy.mokkoji.api.favorite.dto.response.RecruitClubsResponse;
import com.greedy.mokkoji.api.favorite.service.FavoriteService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<Void>> addFavorite(
            @Authentication final AuthCredential authCredential,
            @PathVariable(name = "clubId") final Long clubId
    ) {
        return APISuccessResponse.of(HttpStatus.CREATED, favoriteService.addFavorite(authCredential.userId(), clubId));
    }

    @GetMapping
    public ResponseEntity<APISuccessResponse<ClubsPaginationResponse>> getFavoriteClubs(
            @Authentication final AuthCredential authCredential,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size
    ) {
        final Pageable pageable = PageRequest.of(page - 1, size);

        return APISuccessResponse.of(HttpStatus.OK, favoriteService.findFavoriteClubs(authCredential.userId(), pageable));
    }

    @DeleteMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<Void>> deleteFavorite(
            @Authentication final AuthCredential authCredential,
            @PathVariable(name = "clubId") final Long clubId
    ) {
        return APISuccessResponse.of(HttpStatus.NO_CONTENT, favoriteService.deleteFavorite(authCredential.userId(), clubId));
    }

    @GetMapping("/recruit")
    public ResponseEntity<APISuccessResponse<List<RecruitClubsResponse>>> getRecruitClubs(
            @Authentication final AuthCredential authCredential,
            @RequestParam(name = "yearMonth") YearMonth yearMonth
    ) {
        return APISuccessResponse.of(HttpStatus.OK, favoriteService.getRecruitClubs(authCredential.userId(), yearMonth));
    }
}
