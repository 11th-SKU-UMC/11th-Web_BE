package com.umc.study.global.exception;

public record ErrorResponse(
        int status,
        String message
) {}
