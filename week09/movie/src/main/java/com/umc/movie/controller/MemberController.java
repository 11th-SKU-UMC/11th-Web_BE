package com.umc.movie.controller;

import com.umc.movie.annotation.LoginMember;
import com.umc.movie.config.SwaggerConfig;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.EmailCheckResponse;
import com.umc.movie.dto.MemberInfoResponse;
import com.umc.movie.dto.MemberResponse;
import com.umc.movie.dto.MemberUpdateRequest;
import com.umc.movie.dto.NicknameCheckResponse;
import com.umc.movie.dto.SignUpRequest;
import com.umc.movie.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Tag(name = "회원", description = "회원가입과 내 정보 API")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/nickname/{nickname}")
    public NicknameCheckResponse checkNickname(@PathVariable String nickname) {
        return memberService.checkNickname(nickname);
    }

    @GetMapping("/email/{email}")
    public EmailCheckResponse checkEmail(@PathVariable String email) {
        return memberService.checkEmail(email);
    }

    @Operation(
            summary = "회원가입",
            description = "이메일, 비밀번호, 닉네임으로 회원을 등록합니다. 토큰이 없어도 호출할 수 있는 공개 API입니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        MemberResponse response = memberService.signUp(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("회원가입에 성공했습니다.", response));
    }

    @Operation(
            summary = "내 정보 조회",
            description = """
                    토큰의 주인인 회원의 정보를 조회합니다. 토큰이 필요합니다.

                    | 상태 | code | 상황 |
                    |---|---|---|
                    | 200 | COMMON200 | 조회 성공 |
                    | 401 | AUTH401_2 / AUTH401_3 / AUTH401_4 | 토큰 없음 / 위조 / 만료 |
                    | 404 | MEMBER404_1 | 토큰은 유효하지만 회원이 탈퇴 등으로 존재하지 않음 |
                    """
    )
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMyInfo(
            @LoginMember Long memberId
    ) {
        MemberInfoResponse response = memberService.getMyInfo(memberId);
        return ResponseEntity.ok(ApiResponse.ok("회원 정보를 조회했습니다.", response));
    }

    @Operation(
            summary = "내 정보 수정",
            description = """
                    토큰의 주인인 회원의 닉네임을 수정합니다. 토큰이 필요합니다.

                    | 상태 | code | 상황 |
                    |---|---|---|
                    | 200 | COMMON200 | 수정 성공 |
                    | 400 | COMMON4001 | 닉네임 누락 또는 2~12자 범위 밖 |
                    | 401 | AUTH401_2 / AUTH401_3 / AUTH401_4 | 토큰 없음 / 위조 / 만료 |
                    | 404 | MEMBER404_1 | 회원이 존재하지 않음 |
                    | 409 | MEMBER409_1 | 이미 사용 중인 닉네임 |
                    """
    )
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> updateMyInfo(
            @LoginMember Long memberId,
            @RequestBody @Valid MemberUpdateRequest request
    ) {
        MemberInfoResponse response = memberService.updateMyInfo(memberId, request);
        return ResponseEntity.ok(ApiResponse.ok("회원 정보를 수정했습니다.", response));
    }
}