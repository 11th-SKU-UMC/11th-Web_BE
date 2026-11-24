package com.umc.movie.controller;

import com.umc.movie.annotation.LoginMember;
import com.umc.movie.config.SwaggerConfig;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.RatingPageResponse;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "평점", description = "영화 평점 등록/수정/삭제와 유저별 평점 조회 API")
public class RatingController {

    private final RatingService ratingService;

    @Operation(
            summary = "유저별 평점 목록 조회",
            description = """
                    특정 회원이 남긴 평점을 최신순으로 페이지 단위로 조회합니다. 토큰 없이 호출할 수 있는 공개 API입니다.

                    | 상태 | code | 상황 |
                    |---|---|---|
                    | 200 | - | 조회 성공 (평점이 없으면 빈 목록) |
                    | 400 | COMMON4001 | page가 0 미만이거나 size가 1~50 범위를 벗어남 |
                    | 404 | MEMBER404_1 | 존재하지 않는 회원 |
                    """
    )
    @GetMapping("/members/{memberId}/ratings")
    public RatingPageResponse getMemberRatings(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return ratingService.getRatingsByMember(memberId, page, size);
    }

    @Operation(
            summary = "평점 등록",
            description = """
                    로그인한 회원이 영화 하나에 별점(1~5)과 후기를 남깁니다. 토큰이 필요합니다.

                    | 상태 | code | 상황 |
                    |---|---|---|
                    | 201 | COMMON201 | 등록 성공 |
                    | 400 | COMMON4001 | movieId 누락/0 이하, 별점 누락/범위 밖, 후기 500자 초과 |
                    | 401 | AUTH401_2 / AUTH401_3 / AUTH401_4 | 토큰 없음 / 위조 / 만료 |
                    | 409 | RATING409_1 | 이미 이 영화에 평점을 남김 |
                    """
    )
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PostMapping("/ratings")
    public ResponseEntity<ApiResponse<RatingResponse>> create(
            @LoginMember Long memberId,
            @Valid @RequestBody CreateRatingRequest request) {
        RatingResponse response = ratingService.create(memberId, request);
        return ResponseEntity.status(201).body(ApiResponse.created("평점 등록에 성공했습니다.", response));
    }

    @Operation(
            summary = "평점 수정",
            description = """
                    로그인한 회원이 자신이 남긴 평점의 별점과 후기를 수정합니다. 토큰이 필요합니다.

                    | 상태 | code | 상황 |
                    |---|---|---|
                    | 200 | COMMON200 | 수정 성공 |
                    | 400 | COMMON4001 | 별점 누락/범위 밖, 후기 500자 초과 |
                    | 401 | AUTH401_2 / AUTH401_3 / AUTH401_4 | 토큰 없음 / 위조 / 만료 |
                    | 403 | RATING403_1 | 다른 회원이 작성한 평점 |
                    | 404 | RATING404_1 | 존재하지 않는 평점 |
                    """
    )
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PatchMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<RatingResponse>> update(
            @LoginMember Long memberId,
            @PathVariable Long ratingId,
            @Valid @RequestBody UpdateRatingRequest request) {
        RatingResponse response = ratingService.update(memberId, ratingId, request);
        return ResponseEntity.ok(ApiResponse.ok("평점 수정에 성공했습니다.", response));
    }

    @Operation(
            summary = "평점 삭제",
            description = """
                    로그인한 회원이 자신이 남긴 평점을 삭제합니다. 토큰이 필요하며, 공통 응답 형식을 유지하기 위해 200과 result: null을 반환합니다.

                    | 상태 | code | 상황 |
                    |---|---|---|
                    | 200 | COMMON200 | 삭제 성공 |
                    | 401 | AUTH401_2 / AUTH401_3 / AUTH401_4 | 토큰 없음 / 위조 / 만료 |
                    | 403 | RATING403_1 | 다른 회원이 작성한 평점 |
                    | 404 | RATING404_1 | 존재하지 않는 평점 |
                    """
    )
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @DeleteMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @LoginMember Long memberId,
            @PathVariable Long ratingId) {
        ratingService.delete(memberId, ratingId);
        return ResponseEntity.ok(ApiResponse.ok("평점 삭제에 성공했습니다.", null));
    }
}