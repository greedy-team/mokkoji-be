package com.greedy.mokkoji.api.club.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.club.dto.request.ClubSearchCond;
import com.greedy.mokkoji.api.club.dto.request.ClubUpdateRequest;
import com.greedy.mokkoji.api.club.dto.response.ClubDetailResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubUpdateResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubsPaginationResponse;
import com.greedy.mokkoji.api.club.dto.response.allClubs.AllClubsResponse;
import com.greedy.mokkoji.api.club.service.ClubService;
import com.greedy.mokkoji.api.viewcount.controller.support.ViewCountVisitorResolver;
import com.greedy.mokkoji.api.viewcount.service.ClubViewRecorder;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.university.UniversityCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/clubs")
public class ClubController implements ClubControllerSwagger {

    private final ClubService clubService;
    private final ClubViewRecorder clubViewRecorder;
    private final ViewCountVisitorResolver viewCountVisitorResolver;

    @GetMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<ClubDetailResponse>> getClub(
            @Authentication final AuthCredential authCredential,
            @PathVariable(name = "clubId") final Long clubId
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                clubService.findClub(authCredential.accountId(), clubId)
        );
    }

    @PostMapping("/{clubId}/views")
    public ResponseEntity<Void> recordClubView(
            @Authentication final AuthCredential authCredential,
            @PathVariable(name = "clubId") final Long clubId,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        // 존재 확인 트랜잭션을 끝낸 뒤 Redis를 호출한다.
        clubService.validateClubExists(clubId);
        final String visitorKey = viewCountVisitorResolver.resolveVisitorKey(authCredential, request, response);
        clubViewRecorder.recordClubView(clubId, visitorKey);

        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .build();
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
                        authCredential.accountId(),
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
                clubService.getAllClubs(authCredential.accountId(), universityCode, keyword, affiliation, category, pageable)
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
                        authCredential.authRole(),
                        authCredential.accountId(),
                        clubId,
                        clubUpdateRequest.name(),
                        clubUpdateRequest.category(),
                        clubUpdateRequest.affiliation(),
                        clubUpdateRequest.description(),
                        clubUpdateRequest.logo(),
                        clubUpdateRequest.instagram()
                )
        );
    }
}
