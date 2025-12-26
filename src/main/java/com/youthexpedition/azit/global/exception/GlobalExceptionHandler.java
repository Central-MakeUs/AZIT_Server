package com.youthexpedition.azit.global.exception;

import com.youthexpedition.azit.global.common.response.ApiErrorResponse;
import com.youthexpedition.azit.global.common.response.code.ApiErrorCode;
import com.youthexpedition.azit.global.common.response.code.BaseErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 중 발생하는 커스텀 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getErrorCode().getMessage());
        BaseErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiErrorResponse.of(errorCode));
    }

    /**
     * 400 Error: @Valid 어노테이션으로 DTO 검증 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage());

        return ResponseEntity
                .status(ApiErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ApiErrorResponse.of(ApiErrorCode.INVALID_INPUT_VALUE));
    }

    /**
     * 400 Error: 메서드 파라미터 타입이 일치하지 않을 때 발생 (예: 숫자가 들어와야 하는데 문자가 들어온 경우)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());

        return ResponseEntity
                .status(ApiErrorCode.TYPE_MISMATCH_ERROR.getStatus())
                .body(ApiErrorResponse.of(ApiErrorCode.TYPE_MISMATCH_ERROR));
    }

    /**
     * 405 Error: 지원하지 않는 HTTP 메서드 호출 시 발생 (예: POST인데 GET으로 보낸 경우)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException: {}", e.getMessage());

        return ResponseEntity
                .status(ApiErrorCode.METHOD_NOT_ALLOWED.getStatus())
                .body(ApiErrorResponse.of(ApiErrorCode.METHOD_NOT_ALLOWED));
    }

    /**
     * 500 Error: 그 외 정의되지 않은 모든 서버 내부 에러 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiErrorResponse> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);

        return ResponseEntity
                .status(ApiErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiErrorResponse.of(ApiErrorCode.INTERNAL_SERVER_ERROR));
    }
}