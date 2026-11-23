package com.umc.movie.controller;

import com.umc.movie.config.ApiExamples;
import com.umc.movie.config.OpenApiConfig;
import com.umc.movie.dto.EmailCheckResponse;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.MemberResponse;
import com.umc.movie.dto.NicknameCheckResponse;
import com.umc.movie.dto.SignUpRequest;
import com.umc.movie.dto.MemberInfoResponse;
import com.umc.movie.dto.MemberUpdateRequest;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Tag(name = "회원", description = "공개 회원가입·중복 확인과 인증이 필요한 내 정보 API")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "Bearer 토큰이 필요합니다. 토큰의 주인인 회원의 이메일, 닉네임, 가입 시각을 조회합니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 조회 성공 (COMMON200)", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (AUTH401_2: 토큰 누락, AUTH401_3: 무효, AUTH401_4: 만료)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.UNAUTHORIZED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음 (MEMBER404_1)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.MEMBER_NOT_FOUND)))
    })
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMyInfo(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId) {
        return ResponseEntity.ok(ApiResponse.ok("회원 정보를 조회했습니다.", memberService.getMyInfo(memberId)));
    }

    @PatchMapping("/me")
    @Operation(summary = "내 정보 수정", description = "Bearer 토큰이 필요합니다. 로그인한 회원의 닉네임을 2~12자로 수정합니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 수정 성공 (COMMON200)", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "닉네임 검증 실패 (COMMON4001)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {"isSuccess":false,"code":"COMMON4001","message":"닉네임은 2자 이상 12자 이하여야 합니다.","result":null}
                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (AUTH401_2: 토큰 누락, AUTH401_3: 무효, AUTH401_4: 만료)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.UNAUTHORIZED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음 (MEMBER404_1)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.MEMBER_NOT_FOUND))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "닉네임 중복 (MEMBER409_1)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ApiExamples.NICKNAME_CONFLICT)))
    })
    public ResponseEntity<ApiResponse<MemberInfoResponse>> updateMyInfo(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @RequestBody @Valid MemberUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("회원 정보를 수정했습니다.", memberService.updateMyInfo(memberId, request)));
    }

    @GetMapping("/nickname/{nickname}")
    @Operation(summary = "닉네임 중복 확인", description = "토큰 없이 호출하는 공개 API입니다. 사용 가능한 닉네임이면 available: true를 반환합니다.")
    public NicknameCheckResponse checkNickname(@PathVariable @Size(min = 2, max = 12) String nickname) {
        return memberService.checkNickname(nickname);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "이메일 중복 확인", description = "토큰 없이 호출하는 공개 API입니다. 사용 가능한 이메일이면 available: true를 반환합니다.")
    public EmailCheckResponse checkEmail(@PathVariable @Email String email) {
        return memberService.checkEmail(email);
    }

    @PostMapping
    @Operation(summary = "회원가입", description = "토큰 없이 호출하는 공개 API입니다. 이메일, 비밀번호, 닉네임으로 회원을 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공 (COMMON201)", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 검증 실패 (COMMON4001)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {"isSuccess":false,"code":"COMMON4001","message":"이메일을 입력해주세요.","result":null}
                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이메일 중복 (MEMBER4091), 닉네임 중복 (MEMBER4092)",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "이메일 중복", value = """
                            {"isSuccess":false,"code":"MEMBER4091","message":"이미 사용 중인 이메일입니다.","result":null}
                            """),
                            @ExampleObject(name = "닉네임 중복", value = """
                            {"isSuccess":false,"code":"MEMBER4092","message":"이미 사용 중인 닉네임입니다.","result":null}
                            """ )}))
    })
    public ResponseEntity<ApiResponse<MemberResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        MemberResponse response = memberService.signUp(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("회원가입에 성공했습니다.", response));
    }
}
