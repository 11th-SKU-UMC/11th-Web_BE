package com.example.library.domain.book.exception;

import org.springframework.http.HttpStatus;

public enum BookError {
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "B_001", "존재하지 않는 카테고리입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    BookError(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
