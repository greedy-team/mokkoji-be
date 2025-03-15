package com.greedy.mokkoji.common.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.common.response.APIErrorResponse;
import com.greedy.mokkoji.enums.message.FailMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MokkojiException.class)
    public ResponseEntity<APIErrorResponse> handleMokkojiException(final HttpServletRequest request, final MokkojiException exception) {

        final FailMessage failMessage = exception.getFailMessage();

        log.info("잘못된 요청이 들어왔습니다. URI: {}, 실패 메세지 {}", request.getRequestURI(), failMessage);

        return APIErrorResponse.of(failMessage.getHttpStatus(), failMessage.getCode(), failMessage.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIErrorResponse> handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {

        final String customMessage = exception.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("\n"));
        final FailMessage failMessage = FailMessage.BAD_REQUEST_REQUEST_BODY_VALID;

        return APIErrorResponse.of(failMessage.getHttpStatus(), failMessage.getCode(), customMessage);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<APIErrorResponse> handleMissingParamException(final MissingServletRequestParameterException exception) {

        final String customMessage = "누락된 파라미터 : " + exception.getParameterName();
        final FailMessage failMessage = FailMessage.BAD_REQUEST_MISSING_PARAM;

        log.info("잘못된 요청이 들어왔습니다. 내용:  {}", customMessage);

        return APIErrorResponse.of(failMessage.getHttpStatus(), failMessage.getCode(), customMessage);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<APIErrorResponse> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException exception) {

        final String customMessage = "잘못된 인자 값 : " + exception.getParameter().getParameterName();
        final FailMessage failMessage = FailMessage.BAD_REQUEST_METHOD_ARGUMENT_TYPE;

        return APIErrorResponse.of(failMessage.getHttpStatus(), failMessage.getCode(), customMessage);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<APIErrorResponse> handleHttpMessageNotReadableException(final HttpMessageNotReadableException exception) {

        final FailMessage failMessage = FailMessage.BAD_REQUEST_NOT_READABLE;

        if (exception.getCause() instanceof JsonMappingException jsonMappingException) {

            final String customMessage = jsonMappingException.getPath().stream()
                    .map(ref -> String.format("잘못된 필드 값 : '%s'", ref.getFieldName()))
                    .collect(Collectors.joining("\n"));

            return APIErrorResponse.of(failMessage.getHttpStatus(), failMessage.getCode(), customMessage);
        } else {

            return APIErrorResponse.of(failMessage.getHttpStatus(), failMessage.getCode(), failMessage.getMessage());
        }
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<APIErrorResponse> handleNoResourceFoundException(final NoResourceFoundException exception) {

        final FailMessage failMessage = FailMessage.NOT_FOUND_API;

        return APIErrorResponse.of(failMessage.getHttpStatus(), failMessage.getCode(), failMessage.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<APIErrorResponse> handleNoHandlerFoundException(final NoHandlerFoundException exception) {

        final FailMessage failMessage = FailMessage.NOT_FOUND_API;

        return APIErrorResponse.of(failMessage.getHttpStatus(), failMessage.getCode(), failMessage.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<APIErrorResponse> handleHttpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException exception) {

        final FailMessage failMessage = FailMessage.METHOD_NOT_ALLOWED;

        return APIErrorResponse.of(failMessage.getHttpStatus(), failMessage.getCode(), failMessage.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<APIErrorResponse> handleDataIntegrityViolationException(final DataIntegrityViolationException exception) {

        final FailMessage failMessage = FailMessage.CONFLICT_INTEGRITY;

        if (exception.getCause() instanceof ConstraintViolationException constraintViolationException) {

            final String constraintName = constraintViolationException.getConstraintViolations().toString();
            final String customMessage = String.format("제약 조건 '%s' 위반이 발생했습니다.", constraintName);

            return APIErrorResponse.of(failMessage.getHttpStatus(), failMessage.getCode(), customMessage);
        } else {

            return APIErrorResponse.of(failMessage.getHttpStatus(), failMessage.getCode(), failMessage.getMessage());
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIErrorResponse> handleGeneralException(final HttpServletRequest request, final Exception exception) {

        log.error("예상하지 못한 예외가 발생했습니다. URI: {}", request.getRequestURI(), exception);

        final FailMessage failMessage = FailMessage.INTERNAL_SERVER_ERROR;

        return APIErrorResponse.of(failMessage.getHttpStatus(), failMessage.getCode(), failMessage.getMessage());
    }
}
