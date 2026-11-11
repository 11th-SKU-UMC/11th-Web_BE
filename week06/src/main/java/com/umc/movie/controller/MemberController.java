package com.umc.movie.controller;
import com.umc.movie.dto.*;
import com.umc.movie.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/members")
public class MemberController{
 private final MemberService service;
 public MemberController(MemberService service){this.service=service;}
 @GetMapping("/nickname/{nickname}") public CheckResponse nickname(@PathVariable String nickname){return service.nickname(nickname);}
 @GetMapping("/email/{email}") public CheckResponse email(@PathVariable String email){return service.email(email);}
 @PostMapping public ResponseEntity<ApiResponse<MemberResponse>> signUp(@Valid @RequestBody SignUpRequest request){
  return ResponseEntity.status(201).body(ApiResponse.created("회원가입에 성공했습니다.",service.signUp(request)));
 }
}
