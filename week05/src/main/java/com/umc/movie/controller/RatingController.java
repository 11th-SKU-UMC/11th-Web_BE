package com.umc.movie.controller;
import com.umc.movie.service.RatingService;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/members")
public class RatingController {
 private final RatingService service;
 public RatingController(RatingService service){this.service=service;}
 @GetMapping("/{memberId}/ratings")
 public Object getRatings(@PathVariable Long memberId,@RequestParam(required=false) Integer page,@RequestParam(required=false) Integer size){
  if(page==null && size==null)return service.list(memberId);
  return service.page(memberId,page==null?0:page,size==null?10:size);
 }
}
