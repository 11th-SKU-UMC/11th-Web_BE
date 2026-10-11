package com.umc.movie.dto;

import com.umc.movie.entity.Rating;
import org.springframework.data.domain.Page;

import java.util.List;

public record RatingPageResponse(
        List<RatingResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static RatingPageResponse from(Page<Rating> ratings) {
        return new RatingPageResponse(
                ratings.map(RatingResponse::from).getContent(),
                ratings.getNumber(),
                ratings.getSize(),
                ratings.getTotalElements(),
                ratings.getTotalPages()
        );
    }
}
