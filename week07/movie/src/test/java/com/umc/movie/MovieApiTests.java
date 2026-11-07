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
    @Autowired com.umc.movie.util.JwtUtil jwtUtil;

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
        String response = mvc.perform(post("/ratings").header("Authorization", token(owner))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"movieId\":155,\"score\":5,\"comment\":\"좋아요\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").value("COMMON201"))
                .andReturn().getResponse().getContentAsString();
        Long ratingId = ((Number) JsonPath.read(response, "$.result.ratingId")).longValue();
        mvc.perform(post("/ratings").header("Authorization", token(owner)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":155,\"score\":3}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("RATING409_1"));
        mvc.perform(patch("/ratings/{rating}", ratingId).header("Authorization", token(other))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"score\":1}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("RATING403_1"));
        mvc.perform(delete("/ratings/{rating}", ratingId).header("Authorization", token(other)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("RATING403_1"));
        assertThat(ratings.findById(ratingId).orElseThrow().getScore()).isEqualTo(5);
        mvc.perform(patch("/ratings/{rating}", ratingId).header("Authorization", token(owner))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"score\":4,\"comment\":\"수정한 후기\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("COMMON200"));
        mvc.perform(get("/members/{id}/ratings", owner))
                .andExpect(jsonPath("$[0].score").value(4))
                .andExpect(jsonPath("$[0].comment").value("수정한 후기"))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].updatedAt").exists());
        mvc.perform(delete("/ratings/{rating}", ratingId).header("Authorization", token(owner)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result").isEmpty());
        mvc.perform(get("/members/{id}/ratings", owner)).andExpect(content().json("[]"));
        mvc.perform(patch("/ratings/{rating}", ratingId).header("Authorization", token(owner))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"score\":4}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/ratings/{rating}", ratingId).header("Authorization", token(owner))).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}", "{\"movieId\":0,\"score\":3}", "{\"movieId\":-1,\"score\":3}",
            "{\"movieId\":1,\"score\":0}", "{\"movieId\":1,\"score\":6}",
            "{\"movieId\":1,\"score\":1.5}", "{\"movieId\":1.5,\"score\":3}"
    })
    void invalidRatingIsRejected(String body) throws Exception {
        Long owner = signUp("owner@example.com", "작성자");
        mvc.perform(post("/ratings").header("Authorization", token(owner)).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.isSuccess").value(false));
        assertThat(ratings.count()).isZero();
    }

    @Test
    void commentLimitAndUpdateValidation() throws Exception {
        Long owner = signUp("owner@example.com", "작성자");
        mvc.perform(post("/ratings").header("Authorization", token(owner)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":155,\"score\":5,\"comment\":\"" + "가".repeat(501) + "\"}"))
                .andExpect(status().isBadRequest());
        Rating rating = ratings.saveAndFlush(Rating.builder().member(members.findById(owner).orElseThrow())
                .movieId(155L).score(5).build());
        for (String body : new String[]{"{}", "{\"score\":0}", "{\"score\":6}", "{\"score\":1.5}",
                "{\"score\":4,\"comment\":\"" + "가".repeat(501) + "\"}"}) {
            mvc.perform(patch("/ratings/{rating}", rating.getRatingId()).header("Authorization", token(owner))
                            .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        }
    }

    @Test
    void missingMemberAndInvalidPathReturnUniformErrors() throws Exception {
        mvc.perform(post("/ratings").header("Authorization", token(999999L)).contentType(MediaType.APPLICATION_JSON)
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

    private String token(Long id) {
        return "Bearer " + jwtUtil.createAccessToken(id, "test@example.com");
    }

    @Test
    void loginAndMyInfo() throws Exception {
        Long id = signUp("login@example.com", "로그인회원");
        String response = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"login@example.com\",\"password\":\"Umc!1234\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result.nickname").value("로그인회원"))
                .andExpect(jsonPath("$.result.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String auth = "Bearer " + JsonPath.read(response, "$.result.accessToken");
        mvc.perform(get("/members/me").header("Authorization", auth))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result.email").value("login@example.com"))
                .andExpect(jsonPath("$.result.createdAt").exists())
                .andExpect(jsonPath("$.result.password").doesNotExist());
        for (String nickname : new String[]{"로그인회원", "바꾼닉네임"}) {
            mvc.perform(patch("/members/me").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"" + nickname + "\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.result.nickname").value(nickname));
        }
        assertThat(members.findById(id).orElseThrow().getNickname()).isEqualTo("바꾼닉네임");
        signUp("other@example.com", "다른회원");
        mvc.perform(patch("/members/me").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"다른회원\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("MEMBER409_1"));
        mvc.perform(patch("/members/me").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"한\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/members/me").header("Authorization", token(999999L)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("MEMBER404_1"));
    }

    @Test
    void authenticationFailuresAndPreflight() throws Exception {
        signUp("login@example.com", "로그인회원");
        for (String body : new String[]{
                "{\"email\":\"login@example.com\",\"password\":\"wrong\"}",
                "{\"email\":\"unknown@example.com\",\"password\":\"wrong\"}"}) {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH401_1"));
        }
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/members/me"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH401_2"));
        mvc.perform(post("/ratings").contentType(MediaType.APPLICATION_JSON).content("{\"movieId\":155,\"score\":5}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH401_2"));
        mvc.perform(get("/members/me").header("Authorization", "Bearer tampered"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH401_3"));
        var expired = new com.umc.movie.util.JwtUtil("test-only-movie-rating-secret-key-at-least-32-bytes", -120L);
        mvc.perform(get("/members/me").header("Authorization", "Bearer " + expired.createAccessToken(1L, "a@example.com")))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH401_4"));
        mvc.perform(options("/ratings").header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
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
