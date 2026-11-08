package com.umc.movie;

import com.jayway.jsonpath.JsonPath;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MovieApiTests {
    @Autowired MockMvc mvc;
    @Autowired MemberRepository members;
    @Autowired RatingRepository ratings;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void clean() {
        ratings.deleteAll();
        members.deleteAll();
    }

    @Test
    void signupHashesPasswordAndKeepsDuplicateChecks() throws Exception {
        Long id = signUp("first@example.com", "영화팬");
        Member member = members.findById(id).orElseThrow();
        assertThat(member.getPassword()).hasSize(60).isNotEqualTo("Umc!1234");
        assertThat(encoder.matches("Umc!1234", member.getPassword())).isTrue();
        mvc.perform(get("/members/email/first@example.com"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));
        mvc.perform(get("/members/nickname/영화팬"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));
        mvc.perform(get("/members/email/new@example.com"))
                .andExpect(jsonPath("$.available").value(true));
        mvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson("first@example.com", "다른팬", "Umc!1234")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("MEMBER4091"))
                .andExpect(jsonPath("$.isSuccess").value(false)).andExpect(jsonPath("$.result").isEmpty());
        mvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson("other@example.com", "영화팬", "Umc!1234")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("MEMBER4092"));
    }

    @Test
    void invalidSignupIsRejected() throws Exception {
        for (String body : new String[]{
                signupJson("bad-email", "영화팬", "Umc!1234"),
                signupJson("", "영화팬", "Umc!1234"),
                signupJson("valid@example.com", "한", "Umc!1234"),
                signupJson("valid@example.com", "가".repeat(13), "Umc!1234"),
                signupJson("valid@example.com", "영화팬", "weakpass"),
                signupJson("valid@example.com", "영화팬", "Aa1!" + "가".repeat(24)),
                "{}", "{broken"
        }) {
            mvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON4001"));
        }
        assertThat(members.count()).isZero();
    }

    @Test
    void ratingLifecycleAndOwnership() throws Exception {
        Long owner = signUp("owner@example.com", "작성자");
        Long other = signUp("other@example.com", "다른회원");
        mvc.perform(get("/members/{id}/ratings", owner))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        String response = mvc.perform(post("/members/{id}/ratings", owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"movieId\":155,\"score\":5,\"comment\":\"좋아요\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").value("COMMON201"))
                .andReturn().getResponse().getContentAsString();
        Long ratingId = ((Number) JsonPath.read(response, "$.result")).longValue();
        mvc.perform(post("/members/{id}/ratings", owner).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":155,\"score\":3}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("RATING4091"));
        mvc.perform(patch("/members/{id}/ratings/{rating}", other, ratingId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"score\":1}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/members/{id}/ratings/{rating}", other, ratingId))
                .andExpect(status().isForbidden());
        assertThat(ratings.findById(ratingId).orElseThrow().getScore()).isEqualTo(5);
        mvc.perform(patch("/members/{id}/ratings/{rating}", owner, ratingId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"score\":4,\"comment\":\"수정한 후기\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("COMMON200"));
        mvc.perform(get("/members/{id}/ratings", owner))
                .andExpect(jsonPath("$[0].score").value(4))
                .andExpect(jsonPath("$[0].comment").value("수정한 후기"))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].updatedAt").exists());
        mvc.perform(delete("/members/{id}/ratings/{rating}", owner, ratingId))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get("/members/{id}/ratings", owner)).andExpect(content().json("[]"));
        mvc.perform(patch("/members/{id}/ratings/{rating}", owner, ratingId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"score\":4}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/members/{id}/ratings/{rating}", owner, ratingId)).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}", "{\"movieId\":0,\"score\":3}", "{\"movieId\":-1,\"score\":3}",
            "{\"movieId\":1,\"score\":0}", "{\"movieId\":1,\"score\":6}",
            "{\"movieId\":1,\"score\":1.5}", "{\"movieId\":1.5,\"score\":3}"
    })
    void invalidRatingIsRejected(String body) throws Exception {
        Long owner = signUp("owner@example.com", "작성자");
        mvc.perform(post("/members/{id}/ratings", owner).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.isSuccess").value(false));
        assertThat(ratings.count()).isZero();
    }

    @Test
    void commentLimitAndUpdateValidation() throws Exception {
        Long owner = signUp("owner@example.com", "작성자");
        mvc.perform(post("/members/{id}/ratings", owner).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":155,\"score\":5,\"comment\":\"" + "가".repeat(501) + "\"}"))
                .andExpect(status().isBadRequest());
        Rating rating = ratings.saveAndFlush(Rating.builder().member(members.findById(owner).orElseThrow())
                .movieId(155L).score(5).build());
        for (String body : new String[]{"{}", "{\"score\":0}", "{\"score\":6}", "{\"score\":1.5}",
                "{\"score\":4,\"comment\":\"" + "가".repeat(501) + "\"}"}) {
            mvc.perform(patch("/members/{id}/ratings/{rating}", owner, rating.getRatingId())
                            .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        }
    }

    @Test
    void missingMemberAndInvalidPathReturnUniformErrors() throws Exception {
        mvc.perform(post("/members/999999/ratings").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":155,\"score\":5}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.isSuccess").value(false));
        mvc.perform(get("/members/not-a-number/ratings"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("COMMON4001"));
    }

    @Test
    void databaseAlsoEnforcesUniqueConstraints() throws Exception {
        Long owner = signUp("owner@example.com", "작성자");
        Member member = members.findById(owner).orElseThrow();
        assertThatThrownBy(() -> members.saveAndFlush(Member.builder().email(member.getEmail())
                .password(member.getPassword()).nickname("새회원").build()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> members.saveAndFlush(Member.builder().email("new@example.com")
                .password(member.getPassword()).nickname(member.getNickname()).build()))
                .isInstanceOf(DataIntegrityViolationException.class);
        ratings.saveAndFlush(Rating.builder().member(member).movieId(155L).score(5).build());
        assertThatThrownBy(() -> ratings.saveAndFlush(Rating.builder().member(member).movieId(155L).score(4).build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Long signUp(String email, String nickname) throws Exception {
        String response = mvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson(email, nickname, "Umc!1234")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.result.memberId")).longValue();
    }

    private String signupJson(String email, String nickname, String password) {
        return "{\"email\":\"%s\",\"nickname\":\"%s\",\"password\":\"%s\"}".formatted(email, nickname, password);
    }
}
