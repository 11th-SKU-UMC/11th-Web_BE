package com.umc.movie.controller;

import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.umc.movie.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "평점", description = "영화 평점 등록·조회·수정·삭제 API")
public class RatingController {

    private final RatingService ratingService;

    // 5주차에 만든 유저별 평점 조회입니다. 공개 API라 토큰을 받지 않습니다.
    @Operation(summary = "회원별 평점 조회", description = "토큰 불필요: 회원의 공개 평점 목록을 최신 등록 순서의 배열로 조회합니다. 응답 본문은 공통 래퍼 없이 배열입니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공: 평점 목록 배열 반환"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001: 요청 형식 또는 입력값 오류", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "COMMON4001", "message": "요청 형식이나 값이 올바르지 않습니다.", "result": null}
                    """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MEMBER404_1: 회원을 찾을 수 없음", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "MEMBER404_1", "message": "해당 회원을 찾을 수 없습니다.", "result": null}
                    """)))
    })
    @GetMapping("/members/{memberId}/ratings")
    public List<RatingResponse> getMemberRatings(@PathVariable Long memberId) {
        return ratingService.getRatingsByMember(memberId);
    }

    // 경로에서 /members/{memberId}가 사라지고 memberId는 인터셉터가 담아둔 값을 씁니다
    @Operation(summary = "평점 등록", description = "토큰 필요: Authorization 헤더의 Bearer JWT로 회원을 식별하고 평점을 등록합니다. 요청 본문에 memberId를 보내지 않습니다.")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "COMMON201: 평점 등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001: 요청 형식 또는 입력값 오류", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "COMMON4001", "message": "요청 형식이나 값이 올바르지 않습니다.", "result": null}
                    """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH401_2: 토큰 없음 / AUTH401_3: 유효하지 않은 토큰 / AUTH401_4: 만료된 토큰", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "AUTH401_2", "message": "로그인이 필요한 요청입니다.", "result": null}
                    """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MEMBER404_1: 회원을 찾을 수 없음", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "MEMBER404_1", "message": "해당 회원을 찾을 수 없습니다.", "result": null}
                    """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "RATING409_1: 같은 영화에 평점 중복 등록", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "RATING409_1", "message": "이미 이 영화에 평점을 남기셨습니다.", "result": null}
                    """)))
    })
    @PostMapping("/ratings")
    public ResponseEntity<ApiResponse<RatingResponse>> create(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @RequestBody @Valid CreateRatingRequest request
    ) {
        RatingResponse response = ratingService.create(memberId, request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("평점을 등록했습니다.", response));
    }

    // 경로에서도 /{memberId}가 사라지고 인터셉터가 담아둔 값을 씁니다
    @Operation(summary = "평점 수정", description = "토큰 필요: 작성자 본인의 평점과 한줄평만 수정할 수 있습니다.")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "COMMON200: 평점 수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001: 요청 형식 또는 입력값 오류", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "COMMON4001", "message": "요청 형식이나 값이 올바르지 않습니다.", "result": null}
                    """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH401_2: 토큰 없음 / AUTH401_3: 유효하지 않은 토큰 / AUTH401_4: 만료된 토큰", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "AUTH401_2", "message": "로그인이 필요한 요청입니다.", "result": null}
                    """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "RATING403_1: 다른 회원의 평점 수정·삭제 불가", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "RATING403_1", "message": "본인이 작성한 평점만 수정하거나 삭제할 수 있습니다.", "result": null}
                    """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RATING404_1: 평점을 찾을 수 없음", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "RATING404_1", "message": "존재하지 않는 평점입니다.", "result": null}
                    """)))
    })
    @PatchMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<RatingResponse>> update(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @PathVariable Long ratingId,
            @RequestBody @Valid UpdateRatingRequest request
    ) {
        RatingResponse response = ratingService.update(memberId, ratingId, request);
        return ResponseEntity.ok(ApiResponse.ok("평점을 수정했습니다.", response));
    }

    @Operation(summary = "평점 삭제", description = "토큰 필요: 작성자 본인의 평점만 삭제하고 공통 응답의 result에 null을 반환합니다.")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "COMMON200: 평점 삭제 성공, result null"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001: 요청 형식 또는 입력값 오류", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "COMMON4001", "message": "요청 형식이나 값이 올바르지 않습니다.", "result": null}
                    """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH401_2: 토큰 없음 / AUTH401_3: 유효하지 않은 토큰 / AUTH401_4: 만료된 토큰", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "AUTH401_2", "message": "로그인이 필요한 요청입니다.", "result": null}
                    """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "RATING403_1: 다른 회원의 평점 수정·삭제 불가", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "RATING403_1", "message": "본인이 작성한 평점만 수정하거나 삭제할 수 있습니다.", "result": null}
                    """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RATING404_1: 평점을 찾을 수 없음", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "RATING404_1", "message": "존재하지 않는 평점입니다.", "result": null}
                    """)))
    })
    @DeleteMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @PathVariable Long ratingId
    ) {
        ratingService.delete(memberId, ratingId);
        return ResponseEntity.ok(ApiResponse.ok("평점을 삭제했습니다.", null));
    }
}
