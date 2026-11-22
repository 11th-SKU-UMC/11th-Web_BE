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
import com.umc.movie.dto.*;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Tag(name = "회원", description = "회원가입과 내 정보 API")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 사용 가능 여부를 확인합니다.")
    @GetMapping("/nickname/{nickname}")
    public NicknameCheckResponse checkNickname(@PathVariable String nickname) {
        return memberService.checkNickname(nickname);
    }

    @Operation(summary = "이메일 중복 확인", description = "이메일 사용 가능 여부를 확인합니다.")
    @GetMapping("/email/{email}")
    public EmailCheckResponse checkEmail(@PathVariable String email) {
        return memberService.checkEmail(email);
    }

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임으로 회원을 등록합니다. 토큰 없이 호출하는 공개 API입니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류" , content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = """
                    {"isSuccess": false, "code": "COMMON4001", "message": "입력값 오류", "result": null}
                    """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복" , content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = """
                    {"isSuccess": false, "code": "MEMBER4091", "message": "이메일 또는 닉네임 중복", "result": null}
                    """)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("회원가입에 성공했습니다.", memberService.signUp(request)));
    }
    @Operation(summary = "내 정보 조회", description = "토큰 필요: Bearer JWT에 해당하는 회원의 이메일, 닉네임, 가입 시각을 조회합니다.")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "COMMON200: 내 정보 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH401_2: 토큰 없음 / AUTH401_3: 유효하지 않은 토큰 / AUTH401_4: 만료된 토큰", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "AUTH401_2", "message": "로그인이 필요한 요청입니다.", "result": null}
                    """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MEMBER404_1: 회원을 찾을 수 없음", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "MEMBER404_1", "message": "해당 회원을 찾을 수 없습니다.", "result": null}
                    """)))
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMyInfo(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId) {
        return ResponseEntity.ok(ApiResponse.ok("회원 정보를 조회했습니다.", memberService.getMyInfo(memberId)));
    }

    @Operation(summary = "내 정보 수정", description = "토큰 필요: Bearer JWT에 해당하는 회원의 닉네임을 수정합니다.")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "COMMON200: 내 정보 수정 성공"),
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "MEMBER409_1: 다른 회원이 사용 중인 닉네임", content = @Content(
                mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {"isSuccess": false, "code": "MEMBER409_1", "message": "이미 사용 중인 닉네임입니다.", "result": null}
                    """)))
    })
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> updateMyInfo(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Valid @RequestBody MemberUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("회원 정보를 수정했습니다.", memberService.updateMyInfo(memberId, request)));
    }
}
