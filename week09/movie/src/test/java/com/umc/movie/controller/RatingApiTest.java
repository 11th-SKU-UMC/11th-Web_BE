package com.umc.movie.controller;

import com.umc.movie.entity.Member;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RatingApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired RatingRepository ratingRepository;
    @Autowired JwtUtil jwtUtil;

    private Long memberId;
    private String authorization;
    private static final String RATING_BODY = """
            {"movieId":550,"score":5,"comment":"다시 보고 싶은 영화"}
            """;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(Member.builder()
                .email("rating-api@umc.com").password("encoded-test-password").nickname("평점테스트").build());
        memberId = member.getMemberId();
        authorization = "Bearer " + jwtUtil.createAccessToken(memberId, member.getEmail());
    }

    @Test
    @DisplayName("토큰 없이 POST /ratings를 호출하면 401과 AUTH401_2를 응답하고 평점을 저장하지 않는다")
    void createWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/ratings").contentType(MediaType.APPLICATION_JSON).content(RATING_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("AUTH401_2"))
                .andExpect(jsonPath("$.result").isEmpty());
        assertThat(ratingRepository.existsByMember_MemberIdAndMovieId(memberId, 550L)).isFalse();
    }

    @Test
    @DisplayName("유효한 토큰으로 POST /ratings를 호출하면 201과 COMMON201을 응답하고 토큰 회원의 평점을 저장한다")
    void createWithTokenReturnsCreated() throws Exception {
        mockMvc.perform(post("/ratings").header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(RATING_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.ratingId").isNumber())
                .andExpect(jsonPath("$.result.movieId").value(550))
                .andExpect(jsonPath("$.result.score").value(5));
        assertThat(ratingRepository.existsByMember_MemberIdAndMovieId(memberId, 550L)).isTrue();
    }
}
