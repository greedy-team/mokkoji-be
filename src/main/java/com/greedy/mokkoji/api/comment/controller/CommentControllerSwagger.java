package com.greedy.mokkoji.api.comment.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.comment.dto.request.CommentCreateRequest;
import com.greedy.mokkoji.api.comment.dto.request.CommentUpdateRequest;
import com.greedy.mokkoji.api.comment.dto.response.CommentListResponse;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Comment Controller", description = "댓글 관련 API")
public interface CommentControllerSwagger {

    @Operation(
            summary = "댓글 생성 API",
            description = "평점(`rate`)은 1~5 사이의 정수 값이어야 합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponse(responseCode = "201", description = "댓글 작성 성공")
    ResponseEntity<APISuccessResponse<Void>> createComment(
            @Parameter(name = "clubId", description = "댓글을 작성할 동아리 ID", in = ParameterIn.PATH) Long clubId,
            @Parameter(name = "commentCreateRequest", description = "댓글 생성 요청 본문") @Valid CommentCreateRequest commentCreateRequest,
            @Parameter(hidden = true) AuthCredential authCredential
    );

    @Operation(
            summary = "댓글 목록 조회 API",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공")
    @Parameter(
            name = "clubId",
            description = "조회할 동아리 ID",
            in = ParameterIn.PATH,
            required = true
    )
    ResponseEntity<APISuccessResponse<CommentListResponse>> getComments(
            @Parameter(name = "clubId", description = "조회할 동아리 ID", in = ParameterIn.PATH) Long clubId,
            @Parameter(hidden = true) AuthCredential authCredential
    );

    @Operation(
            summary = "댓글 수정 API",
            description = "평점(`rate`)은 1~5 사이의 정수 값이어야 합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponse(responseCode = "200", description = "댓글 수정 성공")
    ResponseEntity<APISuccessResponse<Void>> updateComment(
            @Parameter(name = "commentId", description = "수정할 댓글 ID", in = ParameterIn.PATH) Long commentId,
            @Parameter(name = "commentUpdateRequest", description = "댓글 수정 요청 본문") @Valid CommentUpdateRequest commentUpdateRequest,
            @Parameter(hidden = true) AuthCredential authCredential
    );

    @Operation(
            summary = "댓글 삭제 API",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponse(responseCode = "200", description = "댓글 삭제 성공")
    ResponseEntity<APISuccessResponse<Void>> deleteComment(
            @Parameter(name = "commentId", description = "삭제할 댓글 ID", in = ParameterIn.PATH) Long commentId,
            @Parameter(hidden = true) AuthCredential authCredential
    );
}

