package com.greedy.mokkoji.common.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record APIErrorResponse(
        int code,
        String message
) {
    public static ResponseEntity<APIErrorResponse> of(final HttpStatus httpStatus, final int code, final String message) {
        return ResponseEntity.status(httpStatus).body(new APIErrorResponse(code, message));
    }
}
