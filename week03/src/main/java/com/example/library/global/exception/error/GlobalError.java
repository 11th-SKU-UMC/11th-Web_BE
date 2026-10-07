package com.example.library.global.exception.error;

import org.springframework.http.HttpStatus;

public enum GlobalError implements ErrorCode {

    INVALID_REQUEST_BODY("요청 본문이 올바르지 않습니다.", HttpStatus.BAD_REQUEST, "G_001"),
    INVALID_VALUE("값이 비어있거나 유효하지 않습니다.", HttpStatus.BAD_REQUEST, "G_002"),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "G_003");

    private final String message;
    private final HttpStatus status;
    private final String code;

    GlobalError(String message, HttpStatus status, String code) {
        this.message = message;
        this.status = status;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }
}
