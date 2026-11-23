package com.umc.movie.controller;

import com.umc.movie.config.ApiExamples;
import com.umc.movie.config.OpenApiConfig;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.RatingPageResponse;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "평점", description = "평점 작성·수정·삭제와 공개 유저별 평점 조회 API")
public class RatingController {
    private final RatingService ratingService;

    @GetMapping("/members/{memberId}/ratings")
    @Operation(summary = "유저별 평점 조회", description = "토큰 없이 조회하는 공개 API입니다. 평점을 최신 등록 순으로 페이지 조회합니다. page는 0 이상, size는 1~100입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "평점 페이지 조회 성공", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 페이지 범위 (COMMON400)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.INVALID_PAGE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음 (MEMBER404_1)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.MEMBER_NOT_FOUND)))
    })
    public RatingPageResponse getMemberRatings(@PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ratingService.getRatingsByMember(memberId, page, size);
    }

    @PostMapping("/ratings")
    @Operation(summary = "평점 등록", description = "Bearer 토큰이 필요합니다. 로그인한 회원이 영화에 1~5점의 평점과 선택 후기를 등록합니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "평점 등록 성공 (COMMON201)", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 검증 실패 (COMMON4001)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.RATING_VALIDATION_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (AUTH401_2: 토큰 누락, AUTH401_3: 무효, AUTH401_4: 만료)",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "토큰 누락", value = ApiExamples.UNAUTHORIZED),
                            @ExampleObject(name = "유효하지 않은 토큰", value = ApiExamples.INVALID_TOKEN),
                            @ExampleObject(name = "토큰 만료", value = ApiExamples.EXPIRED_TOKEN)})),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음 (MEMBER404_1)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.MEMBER_NOT_FOUND))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "같은 영화에 중복 평점 (RATING409_1)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.RATING_CONFLICT)))
    })
    public ResponseEntity<ApiResponse<RatingResponse>> create(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Valid @RequestBody CreateRatingRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("평점을 등록했습니다.", ratingService.create(memberId, request)));
    }

    @PatchMapping("/ratings/{ratingId}")
    @Operation(summary = "평점 수정", description = "Bearer 토큰이 필요합니다. 본인이 작성한 평점의 별점과 후기를 수정합니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "평점 수정 성공 (COMMON200)", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 검증 실패 (COMMON4001)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.RATING_VALIDATION_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (AUTH401_2: 토큰 누락, AUTH401_3: 무효, AUTH401_4: 만료)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.UNAUTHORIZED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "다른 회원의 평점 (RATING403_1)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.RATING_FORBIDDEN))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "평점 없음 (RATING404_1)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.RATING_NOT_FOUND)))
    })
    public ResponseEntity<ApiResponse<RatingResponse>> update(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @PathVariable Long ratingId,
            @Valid @RequestBody UpdateRatingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("평점을 수정했습니다.",
                ratingService.update(ratingId, memberId, request)));
    }

    @DeleteMapping("/ratings/{ratingId}")
    @Operation(summary = "평점 삭제", description = "Bearer 토큰이 필요합니다. 본인이 작성한 평점만 삭제합니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "평점 삭제 성공 (COMMON200)", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (AUTH401_2: 토큰 누락, AUTH401_3: 무효, AUTH401_4: 만료)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.UNAUTHORIZED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "다른 회원의 평점 (RATING403_1)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.RATING_FORBIDDEN))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "평점 없음 (RATING404_1)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.RATING_NOT_FOUND)))
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @PathVariable Long ratingId) {
        ratingService.delete(ratingId, memberId);
        return ResponseEntity.ok(ApiResponse.ok("평점을 삭제했습니다.", null));
    }
}
