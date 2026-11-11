package com.umc.movie.service;
import com.umc.movie.dto.*;
import com.umc.movie.entity.*;
import com.umc.movie.exception.BusinessException;
import com.umc.movie.repository.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
public class RatingService{
 private final RatingRepository ratings;private final MemberRepository members;
 public RatingService(RatingRepository ratings,MemberRepository members){this.ratings=ratings;this.members=members;}
 private Member requireMember(Long id){return members.findById(id).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"MEMBER404","존재하지 않는 회원입니다."));}
 @Transactional(readOnly=true) public List<RatingResponse> list(Long memberId){
  requireMember(memberId);
  return ratings.findByMember_MemberIdOrderByRatingIdDesc(memberId).stream().map(RatingResponse::from).toList();
 }
 @Transactional(readOnly=true) public RatingPageResponse page(Long memberId,int page,int size){
  requireMember(memberId);
  if(page<0||size<1||size>100)throw new BusinessException(HttpStatus.BAD_REQUEST,"COMMON4001","page는 0 이상, size는 1~100이어야 합니다.");
  return RatingPageResponse.from(ratings.findByMember_MemberId(memberId,PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"ratingId"))).map(RatingResponse::from));
 }
 @Transactional public Long create(Long memberId,CreateRatingRequest request){
  Member member=requireMember(memberId);
  if(ratings.existsByMember_MemberIdAndMovieId(memberId,request.movieId()))throw new BusinessException(HttpStatus.CONFLICT,"RATING4091","이미 이 영화에 평점을 남겼습니다.");
  return ratings.saveAndFlush(new Rating(member,request.movieId(),request.score(),request.comment())).getRatingId();
 }
 @Transactional public void update(Long ratingId,Long memberId,UpdateRatingRequest request){
  Rating rating=findOwnedRating(ratingId,memberId);rating.update(request.score(),request.comment());
 }
 @Transactional public void delete(Long ratingId,Long memberId){ratings.delete(findOwnedRating(ratingId,memberId));}
 private Rating findOwnedRating(Long ratingId,Long memberId){
  Rating rating=ratings.findById(ratingId).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"RATING404","존재하지 않는 평점입니다."));
  if(!rating.getMember().getMemberId().equals(memberId))throw new BusinessException(HttpStatus.FORBIDDEN,"RATING403","본인이 작성한 평점만 수정하거나 삭제할 수 있습니다.");
  return rating;
 }
}
