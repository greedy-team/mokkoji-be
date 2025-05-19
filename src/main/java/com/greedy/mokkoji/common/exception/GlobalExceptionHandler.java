package com.greedy.mokkoji.common.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.greedy.mokkoji.common.log.CommonLogInformation;
import com.greedy.mokkoji.common.response.APIErrorResponse;
import com.greedy.mokkoji.enums.message.FailMessage;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final CommonLogInformation commonLogInformation;

    @ExceptionHandler(MokkojiException.class)
    public ResponseEntity<APIErrorResponse> handleMokkojiException(final MokkojiException exception) {

        final FailMessage failMessage = exception.getFailMessage();
        final int failCode = failMessage.getCode();
        final String failMessageMessage = failMessage.getMessage();

        infoLog(failCode, failMessageMessage);

        return APIErrorResponse.of(failMessage.getHttpStatus(), failCode, failMessageMessage);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIErrorResponse> handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {

        final String customMessage = exception.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("\n"));
        final FailMessage failMessage = FailMessage.BAD_REQUEST_REQUEST_BODY_VALID;
        final int failCode = failMessage.getCode();

        infoLog(failCode, customMessage);

        return APIErrorResponse.of(failMessage.getHttpStatus(), failCode, customMessage);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<APIErrorResponse> handleMissingParamException(final MissingServletRequestParameterException exception) {

        final String customMessage = "누락된 파라미터 : " + exception.getParameterName();
        final FailMessage failMessage = FailMessage.BAD_REQUEST_MISSING_PARAM;
        final int failCode = failMessage.getCode();

        infoLog(failCode, customMessage);

        return APIErrorResponse.of(failMessage.getHttpStatus(), failCode, customMessage);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<APIErrorResponse> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException exception) {

        final String customMessage = "잘못된 인자 값 : " + exception.getParameter().getParameterName();
        final FailMessage failMessage = FailMessage.BAD_REQUEST_METHOD_ARGUMENT_TYPE;
        final int failCode = failMessage.getCode();

        infoLog(failCode, customMessage);

        return APIErrorResponse.of(failMessage.getHttpStatus(), failCode, customMessage);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<APIErrorResponse> handleHttpMessageNotReadableException(final HttpMessageNotReadableException exception) {

        final FailMessage failMessage = FailMessage.BAD_REQUEST_NOT_READABLE;
        final int failCode = failMessage.getCode();

        if (exception.getCause() instanceof JsonMappingException jsonMappingException) {

            final String customMessage = jsonMappingException.getPath().stream()
                    .map(ref -> String.format("잘못된 필드 값 : '%s'", ref.getFieldName()))
                    .collect(Collectors.joining("\n"));

            infoLog(failCode, customMessage);

            return APIErrorResponse.of(failMessage.getHttpStatus(), failCode, customMessage);
        } else {

            final String failMessageMessage = failMessage.getMessage();

            infoLog(failCode, failMessageMessage);

            return APIErrorResponse.of(failMessage.getHttpStatus(), failCode, failMessageMessage);
        }
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<APIErrorResponse> handleNoResourceFoundException(final NoResourceFoundException exception) {

        final FailMessage failMessage = FailMessage.NOT_FOUND_API;
        final int failCode = failMessage.getCode();
        final String failMessageMessage = failMessage.getMessage();

        infoLog(failCode, failMessageMessage);


        return APIErrorResponse.of(failMessage.getHttpStatus(), failCode, failMessageMessage);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<APIErrorResponse> handleNoHandlerFoundException(final NoHandlerFoundException exception) {

        final FailMessage failMessage = FailMessage.NOT_FOUND_API;
        final int failCode = failMessage.getCode();
        final String failMessageMessage = failMessage.getMessage();

        infoLog(failCode, failMessageMessage);


        return APIErrorResponse.of(failMessage.getHttpStatus(), failCode, failMessageMessage);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<APIErrorResponse> handleHttpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException exception) {

        final FailMessage failMessage = FailMessage.METHOD_NOT_ALLOWED;
        final int failCode = failMessage.getCode();
        final String failMessageMessage = failMessage.getMessage();

        infoLog(failCode, failMessageMessage);

        return APIErrorResponse.of(failMessage.getHttpStatus(), failCode, failMessageMessage);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<APIErrorResponse> handleDataIntegrityViolationException(final DataIntegrityViolationException exception) {

        final FailMessage failMessage = FailMessage.CONFLICT_INTEGRITY;
        final int failCode = failMessage.getCode();

        if (exception.getCause() instanceof ConstraintViolationException constraintViolationException) {

            final String constraintName = constraintViolationException.getConstraintViolations().toString();
            final String customMessage = String.format("제약 조건 '%s' 위반이 발생했습니다.", constraintName);

            infoLog(failCode, customMessage);

            return APIErrorResponse.of(failMessage.getHttpStatus(), failCode, customMessage);
        } else {

            final String failMessageMessage = failMessage.getMessage();

            infoLog(failCode, failMessageMessage);

            return APIErrorResponse.of(failMessage.getHttpStatus(), failCode, failMessageMessage);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIErrorResponse> handleGeneralException(final Exception exception) {

        final FailMessage failMessage = FailMessage.INTERNAL_SERVER_ERROR;
        final int failCode = failMessage.getCode();
        final String failMessageMessage = failMessage.getMessage();

        log.error("[{}] URI: {}, 실패 코드: {}, 실패 메세지: {}",
                commonLogInformation.getRequestIdentifier(), commonLogInformation.getUri(), failCode, exception.getMessage(), exception
        );

        return APIErrorResponse.of(failMessage.getHttpStatus(), failCode, failMessageMessage);
    }

    private void infoLog(final int failCode, final String failMessage) {
        log.info("[{}] URI: {}, 실패 코드: {}, 실패 메세지: {}",
                commonLogInformation.getRequestIdentifier(), commonLogInformation.getUri(), failCode, failMessage
        );
    }
}
