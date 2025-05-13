package com.greedy.mokkoji.api.comment.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.comment.dto.request.CommentCreateRequest;
import com.greedy.mokkoji.api.comment.dto.request.CommentUpdateRequest;
import com.greedy.mokkoji.api.comment.dto.response.CommentListResponse;
import com.greedy.mokkoji.api.comment.service.CommentService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<Void>> createComment(
            @PathVariable(name = "clubId") final Long clubId,
            @RequestBody final CommentCreateRequest commentCreateRequest,
            @Authentication final AuthCredential authCredential) {
        commentService.createComment(authCredential.userId(), clubId, commentCreateRequest.rate(), commentCreateRequest.content());
        return APISuccessResponse.of(HttpStatus.CREATED, null);
    }

    @GetMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<CommentListResponse>> getComments(
            @PathVariable(name = "clubId") final Long clubId,
            @Authentication final AuthCredential authCredential
    ) {
        return APISuccessResponse.of(HttpStatus.OK, commentService.getComments(authCredential.userId(), clubId));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<APISuccessResponse<Void>> updateComment(
            @PathVariable(name = "commentId") final Long commentId,
            @RequestBody final CommentUpdateRequest commentUpdateRequest,
            @Authentication final AuthCredential authCredential
    ) {
        commentService.updateComment(authCredential.userId(), commentId, commentUpdateRequest.rate(), commentUpdateRequest.content());
        return APISuccessResponse.of(HttpStatus.OK, null);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<APISuccessResponse<Void>> deleteComment(
            @PathVariable(name = "commentId") final Long commentId,
            @Authentication final AuthCredential authCredential
    ) {
        commentService.deleteComment(authCredential.userId(), commentId);
        return APISuccessResponse.of(HttpStatus.OK, null);
    }
}
