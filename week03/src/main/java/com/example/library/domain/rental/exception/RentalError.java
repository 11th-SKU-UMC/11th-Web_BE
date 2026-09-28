package com.example.library.domain.rental.exception;

import com.example.library.global.exception.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum RentalError implements ErrorCode {

    INVALID_RENTAL_REQUEST("userId와 bookId는 1 이상의 숫자여야 합니다.", HttpStatus.BAD_REQUEST, "R_001"),
    USER_OR_BOOK_NOT_FOUND("존재하지 않는 사용자 또는 도서입니다.", HttpStatus.NOT_FOUND, "R_002");

    private final String message;
    private final HttpStatus status;
    private final String code;

    RentalError(String message, HttpStatus status, String code) {
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
