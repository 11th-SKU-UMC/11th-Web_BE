package com.umc.movie.controller;

import com.umc.movie.dto.*;
import com.umc.movie.service.RatingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members/{memberId}/ratings")
public class RatingController {
    private final RatingService ratingService;

    @GetMapping
    public List<RatingResponse> getMemberRatings(@PathVariable Long memberId) {
        return ratingService.getRatingsByMember(memberId);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(@PathVariable Long memberId,
                                                   @Valid @RequestBody CreateRatingRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("평점 등록에 성공했습니다.", ratingService.create(memberId, request)));
    }

    @PatchMapping("/{ratingId}")
    public ApiResponse<Void> update(@PathVariable Long memberId, @PathVariable Long ratingId,
                                    @Valid @RequestBody UpdateRatingRequest request) {
        ratingService.update(ratingId, memberId, request);
        return ApiResponse.ok("평점 수정에 성공했습니다.", null);
    }

    @DeleteMapping("/{ratingId}")
    public ResponseEntity<Void> delete(@PathVariable Long memberId, @PathVariable Long ratingId) {
        ratingService.delete(ratingId, memberId);
        return ResponseEntity.noContent().build();
    }
}
