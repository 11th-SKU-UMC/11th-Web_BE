package com.umc.movie.service;
import com.umc.movie.dto.*;
import com.umc.movie.repository.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@Service
public class RatingService {
 private final RatingRepository ratings;
 private final MemberRepository members;
 public RatingService(RatingRepository ratings,MemberRepository members){this.ratings=ratings;this.members=members;}
 private void ensureMember(Long id){if(!members.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"존재하지 않는 유저입니다.");}
 @Transactional(readOnly=true)
 public List<RatingResponse> list(Long memberId){
  ensureMember(memberId);
  return ratings.findByMember_MemberIdOrderByRatingIdDesc(memberId).stream().map(RatingResponse::from).toList();
 }
 @Transactional(readOnly=true)
 public RatingPageResponse page(Long memberId,int page,int size){
  ensureMember(memberId);
  if(page<0||size<1||size>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"page는 0 이상, size는 1~100이어야 합니다.");
  var pageable=PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"ratingId"));
  return RatingPageResponse.from(ratings.findByMember_MemberId(memberId,pageable).map(RatingResponse::from));
 }
}
