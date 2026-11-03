package com.umc.movie.controller;

import com.umc.movie.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(ApiResponse.failure("COMMON4001", message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException exception) {
        String message = exception.getMostSpecificCause().getMessage();

        if (message != null) {
            if (message.contains("email")) {
                return ResponseEntity.status(409)
                        .body(ApiResponse.failure("MEMBER4091", "이미 사용 중인 이메일입니다."));
            }

            if (message.contains("nickname")) {
                return ResponseEntity.status(409)
                        .body(ApiResponse.failure("MEMBER4092", "이미 사용 중인 닉네임입니다."));
            }
        }

        return ResponseEntity.status(409)
                .body(ApiResponse.failure("COMMON4091", "이미 존재하는 데이터입니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        String message = exception.getMessage();

        if ("이미 사용 중인 이메일입니다.".equals(message)) {
            return ResponseEntity.status(409)
                    .body(ApiResponse.failure("MEMBER4091", message));
        }

        if ("이미 사용 중인 닉네임입니다.".equals(message)) {
            return ResponseEntity.status(409)
                    .body(ApiResponse.failure("MEMBER4092", message));
        }

        return ResponseEntity.status(409)
                .body(ApiResponse.failure("COMMON4092", message));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException exception) {
        int status = exception.getStatusCode().value();
        return ResponseEntity.status(status)
                .body(ApiResponse.failure("COMMON" + status, exception.getReason()));
    }
}