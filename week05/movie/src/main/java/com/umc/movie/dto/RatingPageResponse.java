package com.umc.movie.dto;

import com.umc.movie.entity.Rating;
import org.springframework.data.domain.Page;

import java.util.List;

public record RatingPageResponse(
        List<RatingResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static RatingPageResponse from(Page<Rating> ratingPage) {
        return new RatingPageResponse(
                ratingPage.getContent().stream()
                        .map(RatingResponse::from)
                        .toList(),
                ratingPage.getNumber(),
                ratingPage.getSize(),
                ratingPage.getTotalElements(),
                ratingPage.getTotalPages(),
                ratingPage.hasNext()
        );
    }
}