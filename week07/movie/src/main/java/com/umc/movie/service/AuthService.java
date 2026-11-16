package com.umc.movie.service;

import com.umc.movie.dto.LoginRequest;
import com.umc.movie.dto.LoginResponse;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; // 6주차 회원가입에서 등록해둔 Bean입니다
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        // 1. 이메일로 회원을 찾습니다.
        //    없어도 "가입되지 않은 이메일"이라고 알려주지 않고 아래와 같은 예외를 던집니다.
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH401_1",
                        "이메일 또는 비밀번호가 올바르지 않습니다."));

        // 2. 입력한 비밀번호를 저장된 해시와 비교합니다.
        //    matches()가 저장된 해시에서 salt를 꺼내 같은 방식으로 해시한 뒤 대조해줍니다.
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH401_1",
                    "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 3. 검증을 통과했으니 토큰을 발급합니다.
        String accessToken = jwtUtil.createAccessToken(member.getMemberId(), member.getEmail());

        return new LoginResponse(member.getNickname(), accessToken);
    }
}
