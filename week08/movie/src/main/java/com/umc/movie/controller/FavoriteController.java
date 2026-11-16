package com.umc.movie.controller;

import com.umc.movie.annotation.LoginMember;
import com.umc.movie.config.SwaggerConfig;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.dto.FavoriteStatusResponse;
import com.umc.movie.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.List;

@Tag(name = "Favorite", description = "즐겨찾기 API (로그인 필요)")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
@RestController
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(
            summary = "즐겨찾기 등록",
            description = """
                    로그인한 회원의 즐겨찾기에 영화를 추가합니다. 회원은 토큰으로 식별합니다.

                    | 상태 | code | 상황 |
                    |---|---|---|
                    | 201 | COMMON201 | 등록 성공 |
                    | 400 | COMMON4001 | movieId 누락 또는 0 이하 |
                    | 401 | AUTH401_2 / AUTH401_3 / AUTH401_4 | 토큰 없음 / 위조 / 만료 |
                    | 409 | FAVORITE409_1 | 이미 즐겨찾기한 영화 |
                    """
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/favorites")
    public ResponseEntity<ApiResponse<FavoriteResponse>> create(
            @LoginMember Long memberId,
            @RequestBody @Valid CreateFavoriteRequest request
    ) {
        FavoriteResponse response = favoriteService.create(memberId, request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("즐겨찾기를 등록했습니다.", response));
    }

    @Operation(
            summary = "내 즐겨찾기 목록 조회",
            description = """
                    로그인한 회원의 즐겨찾기를 최신 등록순으로 조회합니다. 없으면 빈 배열을 반환합니다.

                    | 상태 | code | 상황 |
                    |---|---|---|
                    | 200 | COMMON200 | 조회 성공 (빈 목록 포함) |
                    | 401 | AUTH401_2 / AUTH401_3 / AUTH401_4 | 토큰 없음 / 위조 / 만료 |
                    """
    )
    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getMyFavorites(
            @LoginMember Long memberId
    ) {
        List<FavoriteResponse> response = favoriteService.getMyFavorites(memberId);
        return ResponseEntity.ok(ApiResponse.ok("내 즐겨찾기 목록을 조회했습니다.", response));
    }

    @Operation(
            summary = "특정 영화 즐겨찾기 여부 조회",
            description = """
                    로그인한 회원이 해당 영화를 즐겨찾기했는지 확인합니다. 하지 않았어도 오류가 아니라 isFavorite: false를 반환합니다.

                    | 상태 | code | 상황 |
                    |---|---|---|
                    | 200 | COMMON200 | 조회 성공 (true 또는 false) |
                    | 401 | AUTH401_2 / AUTH401_3 / AUTH401_4 | 토큰 없음 / 위조 / 만료 |
                    """
    )
    @GetMapping("/favorites/{movieId}")
    public ResponseEntity<ApiResponse<FavoriteStatusResponse>> getStatus(
            @LoginMember Long memberId,
            @PathVariable Long movieId
    ) {
        FavoriteStatusResponse response = favoriteService.getStatus(memberId, movieId);
        return ResponseEntity.ok(ApiResponse.ok("즐겨찾기 여부를 조회했습니다.", response));
    }

    @Operation(
            summary = "즐겨찾기 해제",
            description = """
                    로그인한 회원의 즐겨찾기에서 영화를 제거합니다. 공통 응답 형식을 유지하기 위해 200과 result: null을 반환합니다.

                    | 상태 | code | 상황 |
                    |---|---|---|
                    | 200 | COMMON200 | 해제 성공 |
                    | 401 | AUTH401_2 / AUTH401_3 / AUTH401_4 | 토큰 없음 / 위조 / 만료 |
                    | 404 | FAVORITE404_1 | 즐겨찾기하지 않은 영화 |
                    """
    )
    @DeleteMapping("/favorites/{movieId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @LoginMember Long memberId,
            @PathVariable Long movieId
    ) {
        favoriteService.delete(memberId, movieId);
        return ResponseEntity.ok(ApiResponse.ok("즐겨찾기를 해제했습니다.", null));
    }
}