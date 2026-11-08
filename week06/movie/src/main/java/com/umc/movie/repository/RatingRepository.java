package com.umc.movie.repository;

import com.umc.movie.entity.Rating;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    boolean existsByMember_MemberIdAndMovieId(Long memberId, Long movieId);
    List<Rating> findByMember_MemberIdOrderByRatingIdDesc(Long memberId);
}
