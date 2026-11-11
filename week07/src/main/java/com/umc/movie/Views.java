package com.umc.movie;

import java.math.BigDecimal;
import java.time.LocalDateTime;
public final class Views {
 public record Signup(Long memberId,String email,String nickname) { static Signup from(Member m) { return new Signup(m.memberId,m.email,m.nickname); } }
 public record Me(String email,String nickname,LocalDateTime createdAt) { static Me from(Member m) { return new Me(m.email,m.nickname,m.createdAt); } }
 public record Login(String nickname,String accessToken) {}
 public record RatingView(Long ratingId,Long movieId,BigDecimal score,String comment,LocalDateTime createdAt) {
  static RatingView from(Rating r) { return new RatingView(r.ratingId,r.movieId,r.score,r.comment,r.createdAt); }
 }
}
