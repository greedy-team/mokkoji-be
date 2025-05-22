package com.greedy.mokkoji.enums.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FailMessage {

    //400
    BAD_REQUEST(HttpStatus.BAD_REQUEST, 40000, "잘못된 요청입니다."),
    BAD_REQUEST_REQUEST_BODY_VALID(HttpStatus.BAD_REQUEST, 40001, "잘못된 요청본문입니다."),
    BAD_REQUEST_MISSING_PARAM(HttpStatus.BAD_REQUEST, 40002, "필수 파라미터가 없습니다."),
    BAD_REQUEST_METHOD_ARGUMENT_TYPE(HttpStatus.BAD_REQUEST, 40003, "메서드 인자타입이 잘못되었습니다."),
    BAD_REQUEST_NOT_READABLE(HttpStatus.BAD_REQUEST, 40004, "Json 오류 혹은 요청본문 필드 오류 입니다. "),

    //401
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 40100, "인증이 필요합니다."),
    UNAUTHORIZED_EXPIRED(HttpStatus.UNAUTHORIZED, 40101, "토큰 기간이 만료 되었습니다."),
    UNAUTHORIZED_EMPTY_HEADER(HttpStatus.UNAUTHORIZED, 40102, "인증 정보가 없습니다."),
    UNAUTHORIZED_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 40103, "토큰의 정보가 올바르지 않습니다."),

    //403
    FORBIDDEN(HttpStatus.FORBIDDEN, 40300, "권한이 없습니다."),
    FORBIDDEN_REGISTER_CLUB(HttpStatus.FORBIDDEN, 40301, "동아리를 등록할 수 있는 권한이 없습니다."),
    FORBIDDEN_MANAGE_CLUB(HttpStatus.FORBIDDEN, 40302, "동아리를 관리할 수 있는 권한이 없습니다."),
    FORBIDDEN_ALREADY_EXIST_COMMENT(HttpStatus.FORBIDDEN, 40303, "이미 댓글이 존재합니다."),
    FORBIDDEN_NOT_COMMENT_WRITER(HttpStatus.FORBIDDEN, 40304, "댓글을 작성한 사용자가 아닙니다."),

    //404
    NOT_FOUND(HttpStatus.NOT_FOUND, 40400, "리소스를 찾을 수 없습니다."),
    NOT_FOUND_API(HttpStatus.NOT_FOUND, 40401, "잘못된 API입니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, 40402, "유저를 찾을 수 없습니다."),
    NOT_FOUND_CLUB(HttpStatus.NOT_FOUND, 40403, "동아리를 찾을 수 없습니다."),
    NOT_FOUND_FAVORITE(HttpStatus.NOT_FOUND, 40404, "즐겨찾기 한 동아리를 찾을 수 없습니다."),
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, 40405, "댓글을 찾을 수 없습니다."),
    NOT_FOUNT_RECRUITMENT(HttpStatus.NOT_FOUND, 40406, "모집글을 찾을 수 없습니다."),

    //405
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 40500, "잘못된 HTTP 메소드 요청입니다."),

    //409
    CONFLICT(HttpStatus.CONFLICT, 40900, "서버의 현재 상태와 요청이 충돌했습니다."),
    CONFLICT_INTEGRITY(HttpStatus.CONFLICT, 40901, "데이터 무결성 위반입니다."),
    CONFLICT_FAVORITE(HttpStatus.CONFLICT, 40902, "이미 즐겨찾기 목록에 추가된 동아리입니다."),

    //500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50000, "서버 내부 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR_SMTP(HttpStatus.INTERNAL_SERVER_ERROR, 50001, "SMTP 서버 내부 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR_SMTP_MAIL(HttpStatus.INTERNAL_SERVER_ERROR, 50002, "SMTP 메일 생성에 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR_SEJONG_AUTH(HttpStatus.INTERNAL_SERVER_ERROR, 50005, "학생 인증 서버 내부 오류가 발생했습니다."),
    INTERNAL_TOKEN_INIT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50010, "SECRET KEY가 초기화되지 않았습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
