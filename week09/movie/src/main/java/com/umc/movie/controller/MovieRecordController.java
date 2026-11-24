package com.umc.movie.controller;

import com.umc.movie.annotation.LoginMember;
import com.umc.movie.config.SwaggerConfig;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateMovieRecordRequest;
import com.umc.movie.dto.MovieRecordResponse;
import com.umc.movie.service.MovieRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "MovieRecord", description = "영화 기록 API (로그인 필요)")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
@RestController
@RequiredArgsConstructor
public class MovieRecordController {

    private final MovieRecordService movieRecordService;

    @Operation(
            summary = "영화 기록 저장 (평점 + 즐겨찾기)",
            description = """
                    평점을 저장하고, addToFavorites가 true이면 즐겨찾기도 함께 저장합니다.
                    두 저장은 하나의 트랜잭션으로 묶여 있어, 하나라도 실패하면 모두 취소됩니다.

                    | 상태 | code | 상황 |
                    |---|---|---|
                    | 201 | COMMON201 | 저장 성공 (addToFavorites가 false면 favorite은 null) |
                    | 400 | COMMON4001 | 입력값 검증 실패 (별점 범위, 필수값 누락 등) |
                    | 401 | AUTH401_2 / AUTH401_3 / AUTH401_4 | 토큰 없음 / 위조 / 만료 |
                    | 409 | RATING409_1 | 이미 이 영화에 평점을 남김 |
                    | 409 | FAVORITE409_1 | 이미 이 영화를 즐겨찾기함 |
                    """
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/movie-records")
    public ResponseEntity<ApiResponse<MovieRecordResponse>> create(
            @LoginMember Long memberId,
            @RequestBody @Valid CreateMovieRecordRequest request
    ) {
        MovieRecordResponse response = movieRecordService.create(memberId, request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("영화 기록을 저장했습니다.", response));
    }
}