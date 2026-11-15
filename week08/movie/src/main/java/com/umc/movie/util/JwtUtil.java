package com.umc.movie.util;

import com.umc.movie.exception.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final Duration accessExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") Long accessExpiration
    ) {
        // 문자열 비밀키를 HMAC-SHA 서명용 키 객체로 바꿉니다.
        // 이 키가 있어야만 서명을 만들 수 있고 같은 키로만 검증할 수 있습니다.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = Duration.ofSeconds(accessExpiration);
    }

    /**
     * 로그인에 성공한 회원에게 발급할 Access Token을 만듭니다.
     */
    public String createAccessToken(Long memberId, String email) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(memberId)) // 토큰의 주인이 누구인지. 우리는 memberId를 넣습니다
                .claim("email", email)             // 참고용 부가 정보 (없어도 동작합니다)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessExpiration))) // 만료 시각이 없으면 영원히 유효한 토큰이 됩니다
                .signWith(secretKey)               // 이 서명 덕분에 내용을 조작하면 검증에서 걸립니다
                .compact();
    }

    /**
     * 토큰을 검증하고 그 안에 담긴 memberId를 꺼냅니다.
     * 서명이 맞지 않거나 만료된 토큰이면 예외를 던지므로 이 메서드가 값을 돌려줬다면
     * "검증까지 통과한 토큰"이라는 뜻입니다.
     */
    public Long getMemberId(String token) {
        try {
            return Long.valueOf(getClaims(token).getPayload().getSubject());
        } catch (ExpiredJwtException e) {
            // 만료는 사용자가 다시 로그인하면 해결되는 상황이라 따로 구분해줍니다
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH401_4",
                    "토큰이 만료되었습니다. 다시 로그인해주세요.");
        } catch (JwtException | IllegalArgumentException e) {
            // 서명 불일치, 형식 오류 등 그 외 모든 실패
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH401_3",
                    "유효하지 않은 토큰입니다.");
        }
    }

    private Jws<Claims> getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)  // 이 키로 서명을 다시 계산해서 대조합니다
                .clockSkewSeconds(60)   // 서버와 클라이언트 시계가 조금 어긋나도 되도록 60초 여유를 둡니다
                .build()
                .parseSignedClaims(token);
    }
}
