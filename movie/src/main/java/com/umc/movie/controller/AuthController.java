package com.umc.movie.controller;

import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.LoginRequest;
import com.umc.movie.dto.LoginResponse;
import com.umc.movie.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "토큰 없이 호출하는 공개 로그인 API")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인 (공개 API)", description = "토큰 없이 호출합니다. result.accessToken을 복사해 Swagger의 Authorize에 입력하세요.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공 (COMMON200)",
                    useReturnTypeSchema = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {"isSuccess":true,"code":"COMMON200","message":"로그인에 성공했습니다.",
                     "result":{"nickname":"영화팬","accessToken":"실제로 발급된 JWT 문자열"}}
                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 검증 실패 (COMMON4001)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {"isSuccess":false,"code":"COMMON4001","message":"이메일을 입력해주세요.","result":null}
                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "잘못된 계정 정보 (AUTH401_1)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {"isSuccess":false,"code":"AUTH401_1","message":"이메일 또는 비밀번호가 올바르지 않습니다.","result":null}
                    """)))
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("로그인에 성공했습니다.", authService.login(request)));
    }
}
