package com.umc.movie.controller;

import com.umc.movie.service.RatingService;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RatingController.class)
class RatingControllerResponseTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean RatingService ratingService;
    @MockitoBean JwtUtil jwtUtil;

    @Test
    void createReturns201WithRatingId() throws Exception {
        when(jwtUtil.getMemberId("token")).thenReturn(1L);
        when(ratingService.create(eq(1L), any()))
                .thenReturn(new RatingResponse(7L, 10L, 5, "good", null, null));

        mockMvc.perform(post("/ratings")
                        .header("Authorization", "Bearer token")
                        .contentType("application/json")
                        .content("{\"movieId\":10,\"score\":5,\"comment\":\"good\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.ratingId").value(7));
    }

    @Test
    void deleteReturns200WithBody() throws Exception {
        when(jwtUtil.getMemberId("token")).thenReturn(1L);
        mockMvc.perform(delete("/ratings/7").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"));

        verify(ratingService).delete(7L, 1L);
    }

    @Test
    void createWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/ratings")
                        .contentType("application/json")
                        .content("{\"movieId\":10,\"score\":5}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_2"));
    }
}
