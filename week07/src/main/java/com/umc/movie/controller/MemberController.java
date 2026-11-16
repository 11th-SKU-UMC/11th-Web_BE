package com.umc.movie.controller;
import com.umc.movie.dto.*;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor @RequestMapping("/members")
public class MemberController {
 private final MemberService memberService;
 @GetMapping("/me") public ResponseEntity<ApiResponse<MemberInfoResponse>> getMyInfo(@RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId) {
  return ResponseEntity.ok(ApiResponse.ok("회원 정보를 조회했습니다.",memberService.getMyInfo(memberId)));
 }
 @PatchMapping("/me") public ResponseEntity<ApiResponse<MemberInfoResponse>> updateMyInfo(@RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,@Valid @RequestBody MemberUpdateRequest request) {
  return ResponseEntity.ok(ApiResponse.ok("회원 정보를 수정했습니다.",memberService.updateMyInfo(memberId,request)));
 }
 @GetMapping("/nickname/{nickname}") public ApiResponse<NicknameCheckResponse> checkNickname(@PathVariable String nickname) { return ApiResponse.ok("닉네임 중복 확인에 성공했습니다.",memberService.checkNickname(nickname)); }
 @GetMapping("/email/{email}") public ApiResponse<EmailCheckResponse> checkEmail(@PathVariable String email) { return ApiResponse.ok("이메일 중복 확인에 성공했습니다.",memberService.checkEmail(email)); }
 @PostMapping public ResponseEntity<ApiResponse<MemberResponse>> signUp(@Valid @RequestBody SignUpRequest request) { return ResponseEntity.status(201).body(ApiResponse.created("회원가입에 성공했습니다.",memberService.signUp(request))); }
}
