package com.umc.movie.config;

import com.umc.movie.controller.AuthController;
import com.umc.movie.controller.FavoriteController;
import com.umc.movie.controller.GlobalExceptionHandler;
import com.umc.movie.controller.MovieRecordController;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.AuthService;
import com.umc.movie.service.FavoriteService;
import com.umc.movie.service.MovieRecordService;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SwaggerWebConfigurationTest.WebApplication.class, properties = {
        "spring.mvc.pathmatch.matching-strategy=path_pattern_parser",
        "springdoc.override-with-generic-response=false"
})
@AutoConfigureMockMvc
class SwaggerWebConfigurationTest {
    @Autowired MockMvc mvc;
    @Autowired @Qualifier("resourceHandlerMapping") SimpleUrlHandlerMapping resources;
    @MockitoBean FavoriteService favoriteService;
    @MockitoBean MovieRecordService movieRecordService;
    @MockitoBean AuthService authService;
    @MockitoBean JwtUtil jwtUtil;

    @Test
    void swaggerResourcesLoadWithDefaultPathPatternParserWithoutAuthentication() throws Exception {
        assertNotNull(resources.getPatternParser());
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));
        mvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk());
    }

    @Test
    void week8DocumentExposesMissionApisAndHidesInternalMemberId() throws Exception {
        mvc.perform(get("/v3/api-docs/week8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/favorites'].post").exists())
                .andExpect(jsonPath("$.paths['/favorites'].get").exists())
                .andExpect(jsonPath("$.paths['/favorites/{movieId}'].get").exists())
                .andExpect(jsonPath("$.paths['/favorites/{movieId}'].delete").exists())
                .andExpect(jsonPath("$.paths['/movie-records'].post").exists())
                .andExpect(jsonPath("$.paths['/favorites'].post.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/auth/login'].post.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/favorites'].post.parameters").doesNotExist())
                .andExpect(jsonPath("$.paths['/movie-records'].post.parameters").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.CreateMovieRecordRequest.properties.memberId").doesNotExist());
        mvc.perform(post("/movie-records").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_2"));
    }

    // DB를 연결하지 않고 실제 MVC·springdoc 자동 설정과 프로젝트 웹 설정을 초기화합니다.
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
    @Import({WebConfig.class, OpenApiConfig.class, AuthInterceptor.class, FavoriteController.class,
            MovieRecordController.class, AuthController.class, GlobalExceptionHandler.class})
    static class WebApplication {}
}
