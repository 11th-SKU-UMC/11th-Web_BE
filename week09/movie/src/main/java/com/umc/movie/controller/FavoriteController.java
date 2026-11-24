package com.umc.movie.controller;

import com.umc.movie.annotation.LoginMember;
import com.umc.movie.config.SwaggerConfig;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.dto.FavoriteStatusResponse;
import com.umc.movie.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "즐겨찾기", description = "로그인한 회원의 영화 즐겨찾기 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(
            summary = "즐겨찾기 등록",
            description = "토큰의 주인인 회원이 TMDB 영화 ID 하나를 즐겨찾기에 추가합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "즐겨찾기 등록 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "isSuccess": true,
                              "code": "COMMON201",
                              "message": "즐겨찾기를 등록했습니다.",
                              "result": { "favoriteId": 1, "movieId": 550, "createdAt": "2026-09-13T21:00:00" }
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "movieId 누락 또는 0 이하",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "isSuccess": false,
                              "code": "COMMON4001",
                              "message": "영화 ID는 양수여야 합니다.",
                              "result": null
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "토큰 없음 / 위조 / 만료",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "토큰 없음", value = """
                                    {
                                      "isSuccess": false,
                                      "code": "AUTH401_2",
                                      "message": "로그인이 필요한 요청입니다.",
                                      "result": null
                                    }
                                    """),
                            @ExampleObject(name = "위조된 토큰", value = """
                                    {
                                      "isSuccess": false,
                                      "code": "AUTH401_3",
                                      "message": "유효하지 않은 토큰입니다.",
                                      "result": null
                                    }
                                    """),
                            @ExampleObject(name = "만료된 토큰", value = """
                                    {
                                      "isSuccess": false,
                                      "code": "AUTH401_4",
                                      "message": "토큰이 만료되었습니다. 다시 로그인해주세요.",
                                      "result": null
                                    }
                                    """)
                    })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "이미 즐겨찾기한 영화",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "isSuccess": false,
                              "code": "FAVORITE409_1",
                              "message": "이미 이 영화를 즐겨찾기하셨습니다.",
                              "result": null
                            }
                            """)))
    })
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