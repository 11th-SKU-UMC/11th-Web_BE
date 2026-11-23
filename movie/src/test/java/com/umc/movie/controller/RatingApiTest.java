package com.umc.movie.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RatingApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository memberRepository;
    @Autowired RatingRepository ratingRepository;
    @Autowired JwtUtil jwtUtil;

    private Member member;
    private String authorizationHeader;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .email("challenger@umc.com").password("encoded-password").nickname("영화조아").build());
        authorizationHeader = "Bearer " + jwtUtil.createAccessToken(member.getMemberId(), member.getEmail());
    }

    @Test
    @DisplayName("토큰 없이 POST /ratings를 호출하면 401과 AUTH401_2를 응답하고 평점을 저장하지 않는다")
    void createRatingWithoutTokenReturns401() throws Exception {
        long before = ratingRepository.count();
        mockMvc.perform(post("/ratings").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRatingRequest(550L, 5, "좋아요"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("AUTH401_2"));
        assertThat(ratingRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("유효한 토큰으로 POST /ratings를 호출하면 201과 COMMON201을 응답하고 토큰 회원의 평점을 저장한다")
    void createRatingWithTokenReturns201AndPersistsRating() throws Exception {
        String body = mockMvc.perform(post("/ratings")
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRatingRequest(550L, 5, "좋아요"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.movieId").value(550))
                .andExpect(jsonPath("$.result.score").value(5))
                .andReturn().getResponse().getContentAsString();

        Long ratingId = objectMapper.readTree(body).path("result").path("ratingId").asLong();
        Rating saved = ratingRepository.findById(ratingId).orElseThrow();
        assertThat(saved.getMember().getMemberId()).isEqualTo(member.getMemberId());
        assertThat(saved.getMovieId()).isEqualTo(550L);
        assertThat(saved.getComment()).isEqualTo("좋아요");
    }

    @Test
    @DisplayName("같은 영화의 평점을 다시 등록하면 409와 RATING409_1을 응답한다")
    void duplicateRatingReturns409() throws Exception {
        ratingRepository.save(Rating.builder().member(member).movieId(550L).score(4).build());
        mockMvc.perform(post("/ratings").header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRatingRequest(550L, 5, null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RATING409_1"));
    }

    @Test
    @DisplayName("다른 회원의 평점을 수정하면 403과 RATING403_1을 응답하고 별점을 유지한다")
    void updatingAnotherMembersRatingReturns403() throws Exception {
        Member owner = memberRepository.save(Member.builder()
                .email("owner@umc.com").password("encoded-password").nickname("작성자").build());
        Rating rating = ratingRepository.save(Rating.builder().member(owner).movieId(550L).score(3).build());

        mockMvc.perform(patch("/ratings/{ratingId}", rating.getRatingId())
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateRatingRequest(5, "변경"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RATING403_1"));
        assertThat(ratingRepository.findById(rating.getRatingId()).orElseThrow().getScore()).isEqualTo(3);
    }

    @Test
    @DisplayName("유저별 평점 목록은 토큰 없이 조회할 수 있다")
    void memberRatingsArePublic() throws Exception {
        mockMvc.perform(get("/members/{memberId}/ratings", member.getMemberId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
