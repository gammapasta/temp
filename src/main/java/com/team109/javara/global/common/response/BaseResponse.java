package com.team109.javara.global.common.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
    public class BaseResponse<T> {
        private boolean success;
        private String message;
        private int code;
        private T data;

        public BaseResponse() {
        }

        //직접 에러 코드 입력시 사용
        public BaseResponse(boolean success, String message, int code, T data) {
            this.success = success;
            this.message = message;
            this.code = code;
            this.data = data;
        }

        //기존에 정의된 HttpStatus를 사용
        public BaseResponse(boolean success, String message, HttpStatus httpStatus, T data) {
            this.success = success;
            this.message = message;
            this.code = httpStatus.value();
            this.data = data;
        }

        // 성공 응답 메서드
        public static <T> BaseResponse<T> success(String message) {
            return new BaseResponse<>(true, message, HttpStatus.OK.value(), null);
        }
        public static <T> BaseResponse<T> success(String message, T data) {
            return new BaseResponse<>(true, message, HttpStatus.OK.value(), data);
        }
        public static <T> BaseResponse<T> success(String message,HttpStatus httpStatus, T data) {
            return new BaseResponse<>(true, message, httpStatus.value(), data);
        }

        // 실패 응답 메서드 - HttpStatus 활용
        public static <T> BaseResponse<T> fail(String message, HttpStatus httpStatus) {
            return new BaseResponse<>(false, message, httpStatus.value(), null);
        }

}

