package com.umc.movie.repository;

import com.umc.movie.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Page<Rating> findByMember_MemberId(Long memberId, Pageable pageable);
}