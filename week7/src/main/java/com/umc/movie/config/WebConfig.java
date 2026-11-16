package com.umc.movie.config;

import com.umc.movie.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    // 5주차 CORS 설정
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해
                .allowedOrigins("http://localhost:5173") // 이 출처에서 오는 요청만 허용
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    // 7주차 인증 인터셉터 설정
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**") // 모든 경로에 인증 적용
                .excludePathPatterns(
                        "/auth/**",             // 로그인
                        "/members",             // 회원가입
                        "/members/nickname/**", // 닉네임 중복 확인
                        "/members/email/**",    // 이메일 중복 확인
                        "/members/*/ratings",   // 회원별 평점 조회
                        "/error"                // 스프링 내부 오류 처리
                );
    }
}