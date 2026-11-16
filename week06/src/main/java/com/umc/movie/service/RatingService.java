package com.umc.movie.service;
import com.umc.movie.dto.*;
import com.umc.movie.entity.*;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @RequiredArgsConstructor
public class RatingService {
 private final RatingRepository ratingRepository; private final MemberRepository memberRepository;
 @Transactional(readOnly=true) public List<RatingResponse> getRatingsByMember(Long memberId) {
  if(!memberRepository.existsById(memberId)) throw new ApiException(HttpStatus.NOT_FOUND,"MEMBER404","존재하지 않는 회원입니다.");
  return ratingRepository.findByMember_MemberIdOrderByRatingIdDesc(memberId).stream().map(RatingResponse::from).toList();
 }
 @Transactional public Long create(Long memberId, CreateRatingRequest request) {
  Member member=memberRepository.findById(memberId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"MEMBER404","존재하지 않는 회원입니다."));
  if(ratingRepository.existsByMember_MemberIdAndMovieId(memberId,request.movieId())) throw new ApiException(HttpStatus.CONFLICT,"RATING409","이미 해당 영화에 평점을 등록했습니다.");
  return ratingRepository.save(new Rating(member,request.movieId(),request.score(),request.comment())).getRatingId();
 }
 @Transactional public void update(Long ratingId, Long memberId, UpdateRatingRequest request) {
  Rating rating=ratingRepository.findById(ratingId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"RATING404","존재하지 않는 평점입니다."));
  if(!rating.getMember().getMemberId().equals(memberId)) throw new ApiException(HttpStatus.FORBIDDEN,"RATING403","다른 회원의 평점은 수정할 수 없습니다.");
  rating.update(request.score(),request.comment());
 }
 @Transactional public void delete(Long ratingId, Long memberId) {
  Rating rating=ratingRepository.findById(ratingId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"RATING404","존재하지 않는 평점입니다."));
  if(!rating.getMember().getMemberId().equals(memberId)) throw new ApiException(HttpStatus.FORBIDDEN,"RATING403","다른 회원의 평점은 삭제할 수 없습니다.");
  ratingRepository.delete(rating);
 }
}
