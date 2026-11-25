package com.umc.movie.exception;
import com.umc.movie.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice
public class GlobalExceptionHandler {
 @ExceptionHandler(ApiException.class) public ResponseEntity<ApiResponse<Void>> handle(ApiException e) { return ResponseEntity.status(e.getStatus()).body(ApiResponse.error(e.getCode(), e.getMessage())); }
 @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException e) {
  String message=e.getBindingResult().getFieldErrors().stream().map(error -> error.getDefaultMessage()).findFirst().orElse("입력값을 확인해주세요.");
  return ResponseEntity.badRequest().body(ApiResponse.error("COMMON400", message));
 }
 @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<ApiResponse<Void>> conflict(DataIntegrityViolationException e) { return ResponseEntity.status(409).body(ApiResponse.error("COMMON409", "이미 존재하거나 저장할 수 없는 데이터입니다.")); }
}
