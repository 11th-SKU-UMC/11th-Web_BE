package com.umc.movie.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.movie.dto.CreateMovieRecordRequest;
import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
// 테스트에 @Transactional을 붙이면 서비스의 트랜잭션을 제거해도 테스트가 대신 감싸므로 사용하지 않습니다.
class MovieRecordRollbackApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository memberRepository;
    @Autowired RatingRepository ratingRepository;
    @Autowired FavoriteRepository favoriteRepository;
    @Autowired JwtUtil jwtUtil;

    private Member member;
    private String authorizationHeader;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .email("rollback@umc.com").password("encoded-password").nickname("롤백검증").build());
        authorizationHeader = "Bearer " + jwtUtil.createAccessToken(member.getMemberId(), member.getEmail());
    }

    @AfterEach
    void cleanUp() {
        // 실제 커밋을 관찰하는 테스트이므로 정리도 독립된 Repository 트랜잭션으로 실행합니다.
        ratingRepository.deleteAll();
        favoriteRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("이미 즐겨찾기한 영화의 기록 저장은 409·FAVORITE409_1로 실패하고 앞서 저장한 평점도 롤백한다")
    void duplicateFavoriteRollsBackPreviouslySavedRating() throws Exception {
        favoriteRepository.save(Favorite.builder().member(member).movieId(550L).build());
        long ratingCountBefore = ratingRepository.count();
        long favoriteCountBefore = favoriteRepository.count();

        mockMvc.perform(post("/movie-records").header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateMovieRecordRequest(550L, 5, "롤백 대상", true))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("FAVORITE409_1"));

        assertThat(ratingRepository.count()).as("실패한 요청의 평점은 DB에 남지 않아야 한다").isEqualTo(ratingCountBefore);
        assertThat(ratingRepository.existsByMember_MemberIdAndMovieId(member.getMemberId(), 550L)).isFalse();
        assertThat(favoriteRepository.count()).isEqualTo(favoriteCountBefore);
    }

    @Test
    @DisplayName("영화 기록 저장에 성공하면 평점과 즐겨찾기가 모두 실제 DB에 커밋된다")
    void successfulMovieRecordCommitsRatingAndFavorite() throws Exception {
        mockMvc.perform(post("/movie-records").header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateMovieRecordRequest(680L, 4, "커밋 대상", true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("COMMON201"));

        assertThat(ratingRepository.existsByMember_MemberIdAndMovieId(member.getMemberId(), 680L)).isTrue();
        assertThat(favoriteRepository.existsByMember_MemberIdAndMovieId(member.getMemberId(), 680L)).isTrue();
    }
}
