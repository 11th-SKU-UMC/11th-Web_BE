package com.umc.movie;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RatingRepository extends JpaRepository<Rating,Long> {
 boolean existsByMember_MemberIdAndMovieId(Long memberId,Long movieId);
 List<Rating> findByMember_MemberIdOrderByRatingIdDesc(Long memberId);
}
