package com.example.library.global.exception.handler;

import com.example.library.global.exception.error.BusinessException;
import com.example.library.global.exception.error.ErrorCode;
import com.example.library.global.exception.error.ErrorResponse;
import com.example.library.global.exception.error.GlobalError;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(BusinessException exception) {
        return convert(exception.getErrorCode());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestBody() {
        return convert(GlobalError.INVALID_REQUEST_BODY);
    }

    private ResponseEntity<ErrorResponse> convert(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode));
    }
}
