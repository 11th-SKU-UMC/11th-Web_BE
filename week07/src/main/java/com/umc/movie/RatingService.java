package com.umc.movie;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;
@Service @Transactional
public class RatingService {
 private final RatingRepository ratings; private final MemberService members;
 public RatingService(RatingRepository ratings,MemberService members) { this.ratings=ratings; this.members=members; }
 @Transactional(readOnly=true) public List<Views.RatingView> list(Long id) { members.find(id); return ratings.findByMember_MemberIdOrderByRatingIdDesc(id).stream().map(Views.RatingView::from).toList(); }
 public Views.RatingView create(Long id,Requests.CreateRating d) {
  Member member=members.find(id);
  if(ratings.existsByMember_MemberIdAndMovieId(id,d.movieId())) throw duplicate();
  try { return Views.RatingView.from(ratings.saveAndFlush(new Rating(member,d))); }
  catch(DataIntegrityViolationException e) { throw duplicate(); }
 }
 public Views.RatingView update(Long id,Long ratingId,Requests.UpdateRating d) { members.find(id); Rating rating=owned(id,ratingId); rating.update(d); ratings.flush(); return Views.RatingView.from(rating); }
 public void delete(Long id,Long ratingId) { members.find(id); ratings.delete(owned(id,ratingId)); ratings.flush(); }
 private Rating owned(Long id,Long ratingId) {
  Rating r=ratings.findById(ratingId).orElseThrow(()->new ApiException(404,"RATING404_1","해당 평점을 찾을 수 없습니다."));
  if(!r.member.getMemberId().equals(id)) throw new ApiException(403,"RATING403_1","본인이 작성한 평점만 수정하거나 삭제할 수 있습니다.");
  return r;
 }
 private ApiException duplicate() { return new ApiException(409,"RATING409_1","이미 이 영화에 평점을 남기셨습니다."); }
}
