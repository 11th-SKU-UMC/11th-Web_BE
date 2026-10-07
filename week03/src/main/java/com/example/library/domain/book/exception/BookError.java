package com.example.library.domain.book.exception;

import com.example.library.global.exception.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum BookError implements ErrorCode {

    INVALID_CATEGORY_ID("categoryId는 1 이상의 값이어야 합니다.", HttpStatus.BAD_REQUEST, "B_001");

    private final String message;
    private final HttpStatus status;
    private final String code;

    BookError(String message, HttpStatus status, String code) {
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
