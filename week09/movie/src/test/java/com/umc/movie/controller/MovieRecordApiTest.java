package com.umc.movie.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.movie.dto.CreateMovieRecordRequest;
import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.ApiException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MovieRecordApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private FavoriteRepository favoriteRepository;

    private String authorizationHeader;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(Member.builder()
                .email("rollback@umc.com")
                .password("$2a$10$abcdefghijklmnopqrstuv")
                .nickname("롤백검증")
                .build());

        authorizationHeader = "Bearer " + jwtUtil.createAccessToken(member.getMemberId(), member.getEmail());
    }

    @AfterEach
    void tearDown() {
        ratingRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("이미 즐겨찾기한 영화로 영화 기록을 저장하면 FAVORITE409_1로 실패하고 평점도 남지 않는다")
    void createMovieRecordWhenAlreadyFavorite() throws Exception {
        // given
        given(favoriteRepository.existsByMember_MemberIdAndMovieId(anyLong(), anyLong())).willReturn(true);
        long ratingCountBefore = ratingRepository.count();

        // when
        mockMvc.perform(post("/movie-records")
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateMovieRecordRequest(550L, 5, "다시 보고 싶은 영화", true))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FAVORITE409_1"));

        // then
        assertThat(ratingRepository.count()).isEqualTo(ratingCountBefore);
    }

    @Test
    @DisplayName("평점 저장 뒤 즐겨찾기 저장이 실패하면 이미 저장한 평점까지 롤백된다")
    void rollbackRatingWhenFavoriteSaveFails() throws Exception {
        // given
        given(favoriteRepository.save(any(Favorite.class)))
                .willThrow(new ApiException(HttpStatus.CONFLICT, "FAVORITE409_1", "이미 이 영화를 즐겨찾기하셨습니다."));
        long ratingCountBefore = ratingRepository.count();

        // when
        mockMvc.perform(post("/movie-records")
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateMovieRecordRequest(550L, 5, "다시 보고 싶은 영화", true))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FAVORITE409_1"));

        // then
        assertThat(ratingRepository.count()).isEqualTo(ratingCountBefore);
    }
}