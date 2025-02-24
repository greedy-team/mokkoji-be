package com.greedy.mokkoji.api.favorite.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.favorite.service.FavoriteService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/favorites")
public class FavoriteController {

    private static final Long USER_ID = 1L;
    private final FavoriteService favoriteService;

    @PostMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<Void>> addFavorite(
            @Authentication AuthCredential authCredential,
            @PathVariable(name = "clubId") final Long clubId
    ) {
        return APISuccessResponse.of(HttpStatus.CREATED, favoriteService.addFavorite(authCredential.userId(), clubId));
    }

    @DeleteMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<Void>> deleteFavorite(
            @Authentication AuthCredential authCredential,
            @PathVariable(name = "clubId") final Long clubId
    ) {
        return APISuccessResponse.of(HttpStatus.NO_CONTENT, favoriteService.deleteFavorite(authCredential.userId(), clubId));
    }
}
