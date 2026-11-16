package com.umc.movie.controller;
import com.umc.movie.dto.*;
import com.umc.movie.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/members")
public class RatingController{
 private final RatingService service;
 public RatingController(RatingService service){this.service=service;}
 @GetMapping("/{memberId}/ratings") public Object getRatings(@PathVariable Long memberId,@RequestParam(required=false) Integer page,@RequestParam(required=false) Integer size){
  if(page==null&&size==null)return service.list(memberId);
  return service.page(memberId,page==null?0:page,size==null?10:size);
 }
 @PostMapping("/{memberId}/ratings") public ResponseEntity<ApiResponse<Long>> create(@PathVariable Long memberId,@Valid @RequestBody CreateRatingRequest request){
  return ResponseEntity.status(201).body(ApiResponse.created("평점 등록에 성공했습니다.",service.create(memberId,request)));
 }
 @PatchMapping("/{memberId}/ratings/{ratingId}") public ApiResponse<Void> update(@PathVariable Long memberId,@PathVariable Long ratingId,@Valid @RequestBody UpdateRatingRequest request){
  service.update(ratingId,memberId,request);return ApiResponse.ok("평점 수정에 성공했습니다.",null);
 }
 @DeleteMapping("/{memberId}/ratings/{ratingId}") public ResponseEntity<Void> delete(@PathVariable Long memberId,@PathVariable Long ratingId){
  service.delete(ratingId,memberId);return ResponseEntity.noContent().build();
 }
}
