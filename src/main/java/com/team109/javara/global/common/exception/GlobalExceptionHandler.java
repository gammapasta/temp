package com.team109.javara.global.common.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.team109.javara.global.common.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice // 모든 @RestController 에 대한 예외 처리를 담당
public class GlobalExceptionHandler {

    // 범용 비즈니스 로직 예외 처리 핸들러
    @ExceptionHandler(GlobalException.class)
    public BaseResponse<Object> handleBusinessLogicException(GlobalException e) {
        ErrorCode errorCode = e.getErrorCode();
        String message = e.getDetailMessage(); // 예외 생성 시 전달된 상세 메시지 사용
        HttpStatus status = errorCode.getStatus(); // Enum에 정의된 상태 코드 사용
        log.warn("비즈니스 로직 예외 발생 [{}]: {}", status, message);
        return BaseResponse.fail(message, status);
    }


    // @Valid 어노테이션을 유효성 검사 실패 시 발생하는 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<Object> handleValidationExceptions(MethodArgumentNotValidException e) {
        log.warn("유효성 검사 실패: {}", e.getMessage()); // 실패 로그 남기기

        // 에러 메시지들을 모아서 하나의 문자열
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage()) // 필드명과 메시지를 함께 포함
                .collect(Collectors.joining(", "));


        return  BaseResponse.fail(errorMessage, HttpStatus.BAD_REQUEST);
    }

    // AuthService 등에서 던지는 비즈니스 로직 관련 예외 처리 (예: 중복 아이디)
    @ExceptionHandler(IllegalStateException.class)
    public BaseResponse<Object> handleIllegalStateException(IllegalStateException e) {
        log.warn("비즈니스 로직 예외 발생: {}", e.getMessage());
        return BaseResponse.fail(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseResponse<Object> handleEnumParseError(HttpMessageNotReadableException e) {

        //enum 오류 처리
        if (e.getCause() instanceof InvalidFormatException invalidFormatException && invalidFormatException.getTargetType() != null && invalidFormatException.getTargetType().isEnum()) {
            Class<?> enumType = invalidFormatException.getTargetType();
            String allowedValues = Arrays.toString(enumType.getEnumConstants());
            String message = String.format("'%s' 필드에는 %s 값만 입력 가능합니다.", enumType.getSimpleName(), allowedValues);
            return BaseResponse.fail(message, HttpStatus.BAD_REQUEST);
        }

        return BaseResponse.fail("잘못된 요청 형식입니다. 입력 값을 확인해주세요.", HttpStatus.BAD_REQUEST);
    }

    //404 에러
    @ExceptionHandler(NoResourceFoundException.class)
    public BaseResponse<Object> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("요청한 리소스를 찾을 수 없음: {}", e.getMessage());
        return BaseResponse.fail("요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public BaseResponse<Object> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("접근 권한 없음: {}", e.getMessage());
        return BaseResponse.fail("접근 권한이 없습니다.", HttpStatus.FORBIDDEN);
    }



    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public BaseResponse<Object> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("지원하지 않는 HTTP 메소드: {}", e.getMessage());
        return BaseResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}