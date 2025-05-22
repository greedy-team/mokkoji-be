package com.greedy.mokkoji.api.favorite.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.club.dto.club.response.ClubResponse;
import com.greedy.mokkoji.api.favorite.service.FavoriteService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<APISuccessResponse<List<ClubResponse>>> getFavoriteClubs(
            @Authentication final AuthCredential authCredential) {
        return APISuccessResponse.of(HttpStatus.OK, favoriteService.findFavoriteClubs(authCredential.userId()));
    }

    @DeleteMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<Void>> deleteFavorite(
            @Authentication final AuthCredential authCredential,
            @PathVariable(name = "clubId") final Long clubId
    ) {
        return APISuccessResponse.of(HttpStatus.NO_CONTENT, favoriteService.deleteFavorite(authCredential.userId(), clubId));
    }
}
