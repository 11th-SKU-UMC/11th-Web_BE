package com.umc.movie.controller;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.exception.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;
@RestControllerAdvice
public class GlobalExceptionHandler{
 @ExceptionHandler(BusinessException.class)
 ResponseEntity<ApiResponse<Void>> business(BusinessException ex){return ResponseEntity.status(ex.getStatus()).body(ApiResponse.failure(ex.getCode(),ex.getMessage()));}
 @ExceptionHandler(MethodArgumentNotValidException.class)
 ResponseEntity<ApiResponse<Void>> invalid(MethodArgumentNotValidException ex){
  String msg=ex.getBindingResult().getFieldErrors().stream().findFirst().map(e->e.getDefaultMessage()).orElse("잘못된 입력입니다.");
  return ResponseEntity.badRequest().body(ApiResponse.failure("COMMON4001",msg));
 }
 @ExceptionHandler(HttpMessageNotReadableException.class)
 ResponseEntity<ApiResponse<Void>> unreadable(HttpMessageNotReadableException ex){return ResponseEntity.badRequest().body(ApiResponse.failure("COMMON4001","요청 JSON 형식이 올바르지 않습니다."));}
 @ExceptionHandler(DataIntegrityViolationException.class)
 ResponseEntity<ApiResponse<Void>> duplicate(DataIntegrityViolationException ex){
  // DB 최종 UNIQUE 위반을 중복 사전 검사와 동일한 에러 코드로 변환한다.
  // MySQL의 index/constraint 이름을 읽으며, 식별 불가 시 일반 409로 처리한다.
  StringBuilder b=new StringBuilder();Throwable cause=ex;
  while(cause!=null){b.append(' ').append(cause.getMessage());cause=cause.getCause();}
  String msg=b.toString().toLowerCase(Locale.ROOT);
  if(msg.contains("uk_rating_member_movie"))return ResponseEntity.status(409).body(ApiResponse.failure("RATING4091","이미 이 영화에 평점을 남겼습니다."));
  if(msg.contains("nickname"))return ResponseEntity.status(409).body(ApiResponse.failure("MEMBER4092","이미 사용 중인 닉네임입니다."));
  if(msg.contains("email"))return ResponseEntity.status(409).body(ApiResponse.failure("MEMBER4091","이미 사용 중인 이메일입니다."));
  return ResponseEntity.status(409).body(ApiResponse.failure("COMMON4091","데이터 무결성 제약을 위반했습니다."));
 }
 @ExceptionHandler(ResponseStatusException.class)
 ResponseEntity<ApiResponse<Void>> status(ResponseStatusException ex){
  return ResponseEntity.status(ex.getStatusCode()).body(ApiResponse.failure("COMMON"+ex.getStatusCode().value(),ex.getReason()==null?"요청 처리에 실패했습니다.":ex.getReason()));
 }
}
