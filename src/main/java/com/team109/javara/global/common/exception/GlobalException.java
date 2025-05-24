package com.team109.javara.global.common.exception;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException{

    private final ErrorCode errorCode;
    private final String detailMessage; // 기본 메시지 외 추가 상세 정보

    // ErrorCode
    public GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailMessage = errorCode.getMessage();
    }

    // ErrorCode + 상세 메시지
    public GlobalException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

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