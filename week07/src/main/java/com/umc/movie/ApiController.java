package com.umc.movie;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
public class ApiController {
 private final MemberService members; private final RatingService ratings; private final MemberRepository repository;
 public ApiController(MemberService members,RatingService ratings,MemberRepository repository) { this.members=members; this.ratings=ratings; this.repository=repository; }
 @PostMapping("/members") public ResponseEntity<?> signup(@Valid @RequestBody Requests.Signup d) { return ResponseEntity.status(201).body(ApiResponse.created("회원가입이 완료되었습니다.",members.signup(d))); }
 @GetMapping("/members/email/{email}") public Map<String,Boolean> email(@PathVariable String email) { return Map.of("available",!repository.existsByEmail(email.trim())); }
 @GetMapping("/members/nickname/{nickname}") public Map<String,Boolean> nickname(@PathVariable String nickname) { return Map.of("available",!repository.existsByNickname(nickname.trim())); }
 @PostMapping("/auth/login") public ApiResponse<?> login(@Valid @RequestBody Requests.Login d) { return ApiResponse.ok("로그인에 성공했습니다.",members.login(d)); }
 @GetMapping("/members/me") public ApiResponse<?> me(@LoginMember Long id) { return ApiResponse.ok("내 정보를 조회했습니다.",members.me(id)); }
 @PatchMapping("/members/me") public ApiResponse<?> update(@LoginMember Long id,@Valid @RequestBody Requests.UpdateMember d) { return ApiResponse.ok("내 정보를 수정했습니다.",members.update(id,d)); }
 @GetMapping("/members/{memberId}/ratings") public ApiResponse<?> list(@PathVariable Long memberId) { return ApiResponse.ok("평점을 조회했습니다.",ratings.list(memberId)); }
 @PostMapping("/ratings") public ResponseEntity<?> create(@LoginMember Long id,@Valid @RequestBody Requests.CreateRating d) { return ResponseEntity.status(201).body(ApiResponse.created("평점을 등록했습니다.",ratings.create(id,d))); }
 @PatchMapping("/ratings/{ratingId}") public ApiResponse<?> updateRating(@LoginMember Long id,@PathVariable Long ratingId,@Valid @RequestBody Requests.UpdateRating d) { return ApiResponse.ok("평점을 수정했습니다.",ratings.update(id,ratingId,d)); }
 @DeleteMapping("/ratings/{ratingId}") public ApiResponse<?> delete(@LoginMember Long id,@PathVariable Long ratingId) { ratings.delete(id,ratingId); return ApiResponse.ok("평점을 삭제했습니다.",null); }
}
