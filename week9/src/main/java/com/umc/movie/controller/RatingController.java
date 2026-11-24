package com.umc.movie.controller;

import com.umc.movie.config.SwaggerConfig;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "평점", description = "영화 평점 등록, 조회, 수정 및 삭제 API")
@RestController
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // 회원별 평점 조회 (공개 API)
    @Operation(
            summary = "회원별 평점 조회",
            description = "특정 회원이 등록한 영화 평점 목록을 조회합니다. 로그인 없이 이용할 수 있습니다."
    )
    @GetMapping("/members/{memberId}/ratings")
    public List<RatingResponse> getMemberRatings(
            @Parameter(description = "조회할 회원 ID", example = "1")
            @PathVariable Long memberId
    ) {
        return ratingService.getRatingsByMember(memberId);
    }

    // 내 평점 조회 (JWT 인증 필요)
    @Operation(
            summary = "내 평점 조회",
            description = "JWT 토큰으로 인증된 회원의 영화 평점 목록을 조회합니다.",
            security = @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    )
    @GetMapping("/ratings/me")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getMyRatings(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId
    ) {
        List<RatingResponse> response =
                ratingService.getRatingsByMember(memberId);

        return ResponseEntity.ok(
                ApiResponse.ok("내 평점을 조회했습니다.", response)
        );
    }

    // 평점 등록 (JWT 인증 필요)
    @Operation(
            summary = "영화 평점 등록",
            description = "로그인한 회원이 영화에 평점을 등록합니다. 같은 영화에 중복 등록할 수 없습니다.",
            security = @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "평점 등록 성공 (COMMON201)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (AUTH401_2)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "평점 중복 등록 (RATING409_1)"
            )
    })
    @PostMapping("/ratings")
    public ResponseEntity<ApiResponse<RatingResponse>> create(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @RequestBody @Valid CreateRatingRequest request
    ) {
        RatingResponse response = ratingService.create(memberId, request);

        return ResponseEntity.status(201)
                .body(ApiResponse.created("평점을 등록했습니다.", response));
    }

    // 평점 수정 (JWT 인증 필요)
    @Operation(
            summary = "영화 평점 수정",
            description = "본인이 등록한 영화 평점을 수정합니다.",
            security = @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "평점 수정 성공 (COMMON200)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (AUTH401_2)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "다른 회원의 평점 수정 불가 (RATING403_1)"
            )
    })
    @PatchMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<RatingResponse>> update(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Parameter(description = "수정할 평점 ID", example = "1")
            @PathVariable Long ratingId,
            @RequestBody @Valid UpdateRatingRequest request
    ) {
        RatingResponse response =
                ratingService.update(memberId, ratingId, request);

        return ResponseEntity.ok(
                ApiResponse.ok("평점을 수정했습니다.", response)
        );
    }

    // 평점 삭제 (JWT 인증 필요)
    @Operation(
            summary = "영화 평점 삭제",
            description = "본인이 등록한 영화 평점을 삭제합니다.",
            security = @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "평점 삭제 성공 (COMMON200)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (AUTH401_2)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "다른 회원의 평점 삭제 불가 (RATING403_1)"
            )
    })
    @DeleteMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Parameter(description = "삭제할 평점 ID", example = "1")
            @PathVariable Long ratingId
    ) {
        ratingService.delete(memberId, ratingId);

        return ResponseEntity.ok(
                ApiResponse.ok("평점을 삭제했습니다.", null)
        );
    }
}
