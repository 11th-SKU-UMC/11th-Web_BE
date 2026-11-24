package com.umc.movie.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.movie.dto.CreateMovieRecordRequest;
import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import com.umc.movie.util.JwtUtil;
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
    private FavoriteRepository favoriteRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("즐겨찾기 중복 시 409와 FAVORITE409_1을 응답하고 평점 저장을 롤백한다")
    void createMovieRecordRollbackWhenFavoriteDuplicated() throws Exception {

        // 1. 테스트 회원 생성
        Member member = memberRepository.save(
                Member.builder()
                        .email("rollback-test@umc.com")
                        .password("test-password")
                        .nickname("롤백테스터")
                        .build()
        );

        Long memberId = member.getMemberId();
        Long movieId = 99001L;

        // 2. 해당 영화를 미리 즐겨찾기에 등록
        Favorite favorite = Favorite.builder()
                .member(member)
                .movieId(movieId)
                .build();

        favoriteRepository.saveAndFlush(favorite);

        // 3. 요청 전 평점 개수 확인
        long beforeCount = ratingRepository.count();

        // 4. JWT 발급
        String authorizationHeader = "Bearer " +
                jwtUtil.createAccessToken(
                        memberId,
                        member.getEmail()
                );

        // 5. 평점 + 중복 즐겨찾기 등록 요청
        CreateMovieRecordRequest request =
                new CreateMovieRecordRequest(
                        movieId,
                        4,
                        "트랜잭션 롤백 테스트",
                        true
                );

        mockMvc.perform(post("/movie-records")
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("FAVORITE409_1"));

        // 6. 요청 후 평점 개수가 그대로인지 확인
        long afterCount = ratingRepository.count();

        assertThat(afterCount).isEqualTo(beforeCount);

        // 7. 해당 회원의 해당 영화 평점도 남지 않았는지 확인
        assertThat(
                ratingRepository.existsByMember_MemberIdAndMovieId(
                        memberId,
                        movieId
                )
        ).isFalse();
    }
}
