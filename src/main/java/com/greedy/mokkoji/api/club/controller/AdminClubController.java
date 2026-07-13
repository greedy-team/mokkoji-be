package com.greedy.mokkoji.api.club.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.club.dto.response.AdminClubsResponse;
import com.greedy.mokkoji.api.club.service.AdminClubService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.enums.university.UniversityCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/admin/clubs")
public class AdminClubController implements AdminClubControllerSwagger {

    private final AdminClubService adminClubService;

    @GetMapping
    public ResponseEntity<APISuccessResponse<AdminClubsResponse>> getAdminClubs(
            @Authentication final AuthCredential authCredential,
            @RequestParam(required = false) final UniversityCode universityCode,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size
    ) {
        final Pageable pageable = PageRequest.of(page - 1, size);
        return APISuccessResponse.of(
                HttpStatus.OK,
                adminClubService.getAdminClubs(authCredential.authRole(), authCredential.accountId(), universityCode, pageable)
        );
    }
}
