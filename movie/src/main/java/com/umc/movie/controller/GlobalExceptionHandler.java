package com.umc.movie.controller;

import com.umc.movie.dto.ApiResponse;
import com.umc.movie.exception.DuplicateError;
import com.umc.movie.exception.DuplicateResourceException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Pattern MYSQL_DUPLICATE_KEY = Pattern.compile("(?i)for key ['`\"]?([a-z0-9_.]+)");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(ApiResponse.failure("COMMON4001", message));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateResourceException exception) {
        return conflict(exception.error());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException exception) {
        DuplicateError error = duplicateErrorFromDatabase(exception);
        if (error != null) {
            log.warn("DB UNIQUE 제약 위반을 {} 응답으로 변환했습니다.", error.code());
            return conflict(error);
        }
        return ResponseEntity.status(409)
                .body(ApiResponse.failure("COMMON4091", "데이터 제약 조건을 위반했습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(409).body(ApiResponse.failure("COMMON4092", exception.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException exception) {
        int status = exception.getStatusCode().value();
        return ResponseEntity.status(status)
                .body(ApiResponse.failure("COMMON" + status, exception.getReason()));
    }

    private ResponseEntity<ApiResponse<Void>> conflict(DuplicateError error) {
        return ResponseEntity.status(409).body(ApiResponse.failure(error.code(), error.message()));
    }

    private DuplicateError duplicateErrorFromDatabase(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException constraint) {
                DuplicateError error = duplicateErrorForKey(constraint.getConstraintName());
                if (error != null) {
                    return error;
                }
            }
            if (cause instanceof SQLException sql && sql.getErrorCode() == 1062) {
                Matcher matcher = MYSQL_DUPLICATE_KEY.matcher(sql.getMessage());
                if (matcher.find()) {
                    return duplicateErrorForKey(matcher.group(1));
                }
            }
        }
        return null;
    }

    private DuplicateError duplicateErrorForKey(String constraintName) {
        if (constraintName == null) {
            return null;
        }
        String key = constraintName.replace("`", "").replace("\"", "").toLowerCase(Locale.ROOT);
        key = key.substring(key.lastIndexOf('.') + 1);
        return switch (key) {
            case "email", "uk_member_email" -> DuplicateError.EMAIL;
            case "nickname", "uk_member_nickname" -> DuplicateError.NICKNAME;
            case "uk_rating_member_movie" -> DuplicateError.RATING;
            default -> null;
        };
    }
}
