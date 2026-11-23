package com.umc.movie.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @DisplayName("Swagger 진입 주소·화면·JSON 문서는 모두 토큰 없이 접근할 수 있다")
    void swaggerEndpointsArePublic() throws Exception {
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs/movie")).andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs/swagger-config")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("필수 API에는 한 줄 설명과 실제 인증 정책에 맞는 bearerAuth 표시가 있다")
    void missionOperationsDescribeAuthenticationAndHideInjectedMemberId() throws Exception {
        JsonNode document = document();
        String[][] protectedOperations = {
                {"/ratings", "post"}, {"/ratings/{ratingId}", "patch"}, {"/ratings/{ratingId}", "delete"},
                {"/members/me", "get"}, {"/members/me", "patch"},
                {"/favorites", "post"}, {"/favorites", "get"},
                {"/favorites/{movieId}", "get"}, {"/favorites/{movieId}", "delete"}
        };
        for (String[] endpoint : protectedOperations) {
            JsonNode operation = document.path("paths").path(endpoint[0]).path(endpoint[1]);
            assertThat(operation.path("summary").asText()).as(endpoint[0]).isNotBlank();
            assertThat(operation.path("description").asText()).as(endpoint[0]).contains("토큰");
            assertThat(operation.path("security").path(0).has(OpenApiConfig.BEARER_AUTH)).isTrue();
            for (JsonNode parameter : operation.path("parameters")) {
                assertThat(parameter.path("name").asText()).isNotEqualTo("memberId");
            }
            assertThat(operation.path("responses").path("401").path("description").asText()).contains("AUTH401_2");
        }
        String[][] publicOperations = {{"/auth/login", "post"}, {"/members", "post"},
                {"/members/{memberId}/ratings", "get"}};
        for (String[] endpoint : publicOperations) {
            JsonNode operation = document.path("paths").path(endpoint[0]).path(endpoint[1]);
            assertThat(operation.path("summary").asText()).isNotBlank();
            assertThat(operation.path("description").asText()).contains("공개");
            assertThat(operation.path("security").isMissingNode() || operation.path("security").isEmpty()).isTrue();
        }
    }

    @Test
    @DisplayName("평점 실패 응답에는 HTTP 409·403과 RATING409_1·RATING403_1 및 JSON 예시가 함께 있다")
    void ratingErrorsIncludeHttpStatusAndBusinessCode() throws Exception {
        JsonNode paths = document().path("paths");
        assertError(paths.path("/ratings").path("post"), "409", "RATING409_1");
        assertError(paths.path("/ratings/{ratingId}").path("patch"), "403", "RATING403_1");
        assertError(paths.path("/ratings/{ratingId}").path("delete"), "403", "RATING403_1");
    }

    private void assertError(JsonNode operation, String status, String code) throws Exception {
        JsonNode response = operation.path("responses").path(status);
        assertThat(response.path("description").asText()).contains(code);
        JsonNode example = response.path("content").path("application/json").path("example");
        if (example.isTextual()) example = objectMapper.readTree(example.asText());
        assertThat(example.path("code").asText()).isEqualTo(code);
    }

    private JsonNode document() throws Exception {
        return objectMapper.readTree(mockMvc.perform(get("/v3/api-docs/movie"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
}
