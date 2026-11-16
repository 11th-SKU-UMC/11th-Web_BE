package com.umc.movie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateMovieRecordRequest(
        @Schema(description = "TMDB 영화 ID", example = "238")
        @NotNull(message = "영화 ID는 필수입니다.")
        @Positive(message = "영화 ID는 양수여야 합니다.")
        Long movieId,

        @Schema(description = "별점 (1~5)", example = "5")
        @NotNull(message = "별점은 필수입니다.")
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
        Integer score,

        @Schema(description = "한줄평 (최대 500자)", example = "다시 보고 싶은 영화입니다.")
        @Size(max = 500, message = "코멘트는 500자 이하여야 합니다.")
        String comment,

        @Schema(description = "즐겨찾기에도 함께 추가할지 여부", example = "true")
        @NotNull(message = "즐겨찾기 추가 여부는 필수입니다.")
        Boolean addToFavorites
) {
}