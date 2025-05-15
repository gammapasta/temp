package com.team109.javara.global.common.exception;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException{

    private final ErrorCode errorCode;
    private final String detailMessage; // 기본 메시지 외 추가 상세 정보

    // ErrorCode만 받는 생성자 (기본 메시지 사용)
    public GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // RuntimeException의 메시지로 설정
        this.errorCode = errorCode;
        this.detailMessage = errorCode.getMessage();
    }

    // ErrorCode와 상세 메시지를 받는 생성자
    public GlobalException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage); // RuntimeException의 메시지로 상세 메시지 설정
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    // 필요시 Cause를 받는 생성자 추가
    public GlobalException(ErrorCode errorCode, String detailMessage, Throwable cause) {
        super(detailMessage, cause);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }
    public GlobalException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.detailMessage = errorCode.getMessage();
    }
}