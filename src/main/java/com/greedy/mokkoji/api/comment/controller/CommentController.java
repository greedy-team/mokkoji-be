package com.greedy.mokkoji.api.comment.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.comment.dto.request.CommentCreateRequest;
import com.greedy.mokkoji.api.comment.dto.request.CommentUpdateRequest;
import com.greedy.mokkoji.api.comment.dto.response.CommentListResponse;
import com.greedy.mokkoji.api.comment.service.CommentService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/comments")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 생성 API", description = " 평점(`rate`)은 1~5 사이의 정수 값이어야 합니다.", security = @SecurityRequirement(name = "JWT"))
    @ApiResponse(responseCode = "201", description = "댓글 작성 성공")
    @PostMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<Void>> createComment(
            @PathVariable(name = "clubId") final Long clubId,
            @RequestBody @Valid final CommentCreateRequest commentCreateRequest,
            @Authentication final AuthCredential authCredential
    ) {
        commentService.createComment(
                authCredential.userId(),
                clubId,
                commentCreateRequest.rate(),
                commentCreateRequest.content()
        );
        return APISuccessResponse.of(HttpStatus.CREATED, null);
    }

    @Operation(summary = "댓글 목록 조회 API", security = @SecurityRequirement(name = "JWT"))
    @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공")
    @Parameter(
            name = "clubId",
            description = "조회할 동아리 ID",
            in = ParameterIn.PATH,
            required = true
    )
    @GetMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<CommentListResponse>> getComments(
            @PathVariable(name = "clubId") final Long clubId,
            @Authentication final AuthCredential authCredential
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                commentService.getComments(authCredential.userId(), clubId)
        );
    }

    @Operation(summary = "댓글 수정 API", description = " 평점(`rate`)은 1~5 사이의 정수 값이어야 합니다.", security = @SecurityRequirement(name = "JWT"))
    @ApiResponse(responseCode = "200", description = "댓글 수정 성공")
    @PatchMapping("/{commentId}")
    public ResponseEntity<APISuccessResponse<Void>> updateComment(
            @PathVariable(name = "commentId") final Long commentId,
            @RequestBody @Valid final CommentUpdateRequest commentUpdateRequest,
            @Authentication final AuthCredential authCredential
    ) {
        commentService.updateComment(
                authCredential.userId(),
                commentId,
                commentUpdateRequest.rate(),
                commentUpdateRequest.content()
        );
        return APISuccessResponse.of(HttpStatus.OK, null);
    }

    @Operation(summary = "댓글 삭제 API", security = @SecurityRequirement(name = "JWT"))
    @ApiResponse(responseCode = "200", description = "댓글 삭제 성공")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<APISuccessResponse<Void>> deleteComment(
            @PathVariable(name = "commentId") final Long commentId,
            @Authentication final AuthCredential authCredential
    ) {
        commentService.deleteComment(authCredential.userId(), commentId);
        return APISuccessResponse.of(HttpStatus.OK, null);
    }
}
