package com.umc.movie.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI movieOpenApi() {
        return new OpenAPI()
                .info(new Info().title("UMC Movie API").version("8주차")
                        .description("즐겨찾기와 영화 기록 API 문서입니다. /auth/login은 공개 API입니다. "
                                + "즐겨찾기와 영화 기록 API는 Authorization: Bearer {token} 헤더가 필요합니다. "
                                + "로그인 응답의 result.accessToken을 Authorize에 입력하세요. "
                                + "회원은 토큰으로 식별하므로 요청 URL, 쿼리, 바디에 memberId를 보내지 않습니다."))
                .components(new Components().addSecuritySchemes(BEARER_AUTH,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi week8Api() {
        return GroupedOpenApi.builder()
                .group("week8")
                .pathsToMatch("/favorites", "/favorites/**", "/movie-records", "/auth/login")
                .build();
    }
}
