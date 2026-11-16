package com.umc.movie;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.context.request.WebRequest;
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
 @ExceptionHandler(ApiException.class)
 ResponseEntity<ApiResponse<Void>> api(ApiException e) { return ResponseEntity.status(e.status).body(ApiResponse.failure(e.code,e.getMessage())); }
 @Override protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,HttpHeaders h,HttpStatusCode s,WebRequest r) {
  String message=e.getBindingResult().getFieldErrors().stream().findFirst().map(x->x.getDefaultMessage()).orElse("입력값이 올바르지 않습니다.");
  return ResponseEntity.status(400).body(ApiResponse.failure("COMMON400",message));
 }
 @Override protected ResponseEntity<Object> handleExceptionInternal(Exception e,Object b,HttpHeaders h,HttpStatusCode s,WebRequest r) {
  return new ResponseEntity<>(ApiResponse.failure("COMMON"+s.value(),s.value()==400?"요청 형식이 올바르지 않습니다.":"요청을 처리할 수 없습니다."),h,s);
 }
}
