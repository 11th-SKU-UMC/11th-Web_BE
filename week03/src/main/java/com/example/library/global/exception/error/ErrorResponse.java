package com.example.library.global.exception.error;

import java.time.LocalDateTime;

public record ErrorResponse(
        String timeStamp,
        String errorCode,
        String errorMessage,
        Object details
) {

    public ErrorResponse(ErrorCode errorCode) {
        this(
                LocalDateTime.now().toString(),
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
    }

    public ErrorResponse(ErrorCode errorCode, Object details) {
        this(
                LocalDateTime.now().toString(),
                errorCode.getCode(),
                errorCode.getMessage(),
                details
        );
    }
}
