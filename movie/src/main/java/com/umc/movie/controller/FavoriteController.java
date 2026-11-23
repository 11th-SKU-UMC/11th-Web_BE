package com.umc.movie.controller;

import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.dto.FavoriteStatusResponse;
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
import com.umc.movie.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "즐겨찾기", description = "토큰으로 식별한 회원의 즐겨찾기 API")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401", description = "인증 실패 (AUTH401_2: 토큰 누락, AUTH401_3: 무효, AUTH401_4: 만료)",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = {
                        @ExampleObject(name = "토큰 누락", value = ApiExamples.UNAUTHORIZED),
                        @ExampleObject(name = "유효하지 않은 토큰", value = ApiExamples.INVALID_TOKEN),
                        @ExampleObject(name = "토큰 만료", value = ApiExamples.EXPIRED_TOKEN)})))
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/favorites")
    @Operation(summary = "즐겨찾기 등록", description = "Bearer 토큰이 필요합니다. Authorization: Bearer {token}으로 전달하며 memberId는 받지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공 (COMMON201)",
                    useReturnTypeSchema = true, content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = ApiExamples.FAVORITE_CREATED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 검증 실패 (COMMON4001)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = ApiExamples.VALIDATION_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음 (MEMBER404_1)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = ApiExamples.MEMBER_NOT_FOUND))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복 즐겨찾기 (FAVORITE409_1)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FAVORITE_CONFLICT)))
    })
    public ResponseEntity<ApiResponse<FavoriteResponse>> create(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @RequestBody @Valid CreateFavoriteRequest request
    ) {
        FavoriteResponse response = favoriteService.create(memberId, request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("즐겨찾기를 등록했습니다.", response));
    }

    @GetMapping("/favorites")
    @Operation(summary = "내 즐겨찾기 목록", description = "Bearer 토큰이 필요합니다. 토큰 회원의 즐겨찾기를 favoriteId 내림차순(최신 등록 순)으로 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 (COMMON200), 없으면 빈 배열",
            useReturnTypeSchema = true, content = @Content(mediaType = "application/json",
            examples = @ExampleObject(value = ApiExamples.FAVORITE_LIST)))
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getMyFavorites(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId
    ) {
        List<FavoriteResponse> response = favoriteService.getMyFavorites(memberId);
        return ResponseEntity.ok(ApiResponse.ok("내 즐겨찾기 목록을 조회했습니다.", response));
    }

    @GetMapping("/favorites/{movieId}")
    @Operation(summary = "즐겨찾기 여부", description = "Bearer 토큰이 필요합니다. 등록 여부를 isFavorite으로 반환합니다. 미등록 영화도 200과 false를 반환합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 (COMMON200)",
            useReturnTypeSchema = true, content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "등록됨", value = ApiExamples.FAVORITE_TRUE),
            @ExampleObject(name = "미등록", value = ApiExamples.FAVORITE_FALSE)}))
    public ResponseEntity<ApiResponse<FavoriteStatusResponse>> getStatus(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Parameter(description = "TMDB 영화 ID", example = "550") @PathVariable Long movieId
    ) {
        FavoriteStatusResponse response = favoriteService.getStatus(memberId, movieId);
        return ResponseEntity.ok(ApiResponse.ok("즐겨찾기 여부를 조회했습니다.", response));
    }

    @DeleteMapping("/favorites/{movieId}")
    @Operation(summary = "즐겨찾기 해제", description = "Bearer 토큰이 필요합니다. 토큰 회원의 해당 영화 즐겨찾기를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "해제 성공 (COMMON200)",
                    useReturnTypeSchema = true, content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = ApiExamples.FAVORITE_DELETED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "즐겨찾기 없음 (FAVORITE404_1)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FAVORITE_NOT_FOUND)))
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Parameter(description = "TMDB 영화 ID", example = "550") @PathVariable Long movieId
    ) {
        favoriteService.delete(memberId, movieId);
        return ResponseEntity.ok(ApiResponse.ok("즐겨찾기를 해제했습니다.", null));
    }
}
