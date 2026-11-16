package com.umc.movie.controller;

import com.umc.movie.service.RatingService;
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

    @Test
    void createReturns201WithRatingId() throws Exception {
        when(ratingService.create(eq(1L), any())).thenReturn(7L);

        mockMvc.perform(post("/members/1/ratings")
                        .contentType("application/json")
                        .content("{\"movieId\":10,\"score\":5,\"comment\":\"good\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result").value(7));
    }

    @Test
    void deleteReturns204WithoutBody() throws Exception {
        mockMvc.perform(delete("/members/1/ratings/7"))
                .andExpect(status().isNoContent());

        verify(ratingService).delete(7L, 1L);
    }
}
