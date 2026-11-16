package com.umc.movie.controller;

import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateMovieRecordRequest;
import com.umc.movie.dto.MovieRecordResponse;
import com.umc.movie.config.ApiExamples;
import com.umc.movie.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.MovieRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "영화 기록", description = "평점과 선택적 즐겨찾기를 하나의 트랜잭션에서 저장")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class MovieRecordController {

    private final MovieRecordService movieRecordService;

    @PostMapping("/movie-records")
    @Operation(summary = "영화 기록 저장", description = "Authorization: Bearer {token}이 필요하며 memberId는 받지 않습니다. "
            + "addToFavorites=true이면 평점과 즐겨찾기를 함께 저장하고, 하나라도 실패하면 모두 롤백합니다. "
            + "false이면 평점만 저장하고 result.favorite는 null입니다. score는 1~5입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "저장 성공 (COMMON201)",
                    useReturnTypeSchema = true, content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "평점과 즐겨찾기", value = ApiExamples.MOVIE_RECORD_CREATED),
                    @ExampleObject(name = "평점만", value = ApiExamples.MOVIE_RECORD_WITHOUT_FAVORITE)})),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 검증 실패 (COMMON4001)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = ApiExamples.VALIDATION_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (AUTH401_2: 토큰 누락, AUTH401_3: 무효, AUTH401_4: 만료)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 누락", value = ApiExamples.UNAUTHORIZED),
                                    @ExampleObject(name = "유효하지 않은 토큰", value = ApiExamples.INVALID_TOKEN),
                                    @ExampleObject(name = "토큰 만료", value = ApiExamples.EXPIRED_TOKEN)})),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음 (MEMBER404_1)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = ApiExamples.MEMBER_NOT_FOUND))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복 평점 (RATING409_1) 또는 즐겨찾기 (FAVORITE409_1)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {
                    @ExampleObject(name = "중복 평점", value = ApiExamples.RATING_CONFLICT),
                    @ExampleObject(name = "중복 즐겨찾기", value = ApiExamples.FAVORITE_CONFLICT)}))
    })
    public ResponseEntity<ApiResponse<MovieRecordResponse>> create(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @RequestBody @Valid CreateMovieRecordRequest request
    ) {
        MovieRecordResponse response = movieRecordService.create(memberId, request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("영화 기록을 저장했습니다.", response));
    }
}
