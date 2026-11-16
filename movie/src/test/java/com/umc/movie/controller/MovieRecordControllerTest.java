package com.umc.movie.controller;

import com.umc.movie.dto.CreateMovieRecordRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.dto.MovieRecordResponse;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.service.MovieRecordService;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieRecordController.class)
class MovieRecordControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean MovieRecordService movieRecordService;
    @MockitoBean JwtUtil jwtUtil;

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(post("/movie-records").contentType("application/json")
                        .content("{\"movieId\":10,\"score\":4,\"addToFavorites\":true}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(movieRecordService);
    }

    @Test
    void createsRecordForTokenMember() throws Exception {
        when(jwtUtil.getMemberId("token")).thenReturn(1L);
        CreateMovieRecordRequest request = new CreateMovieRecordRequest(10L, 4, null, true);
        when(movieRecordService.create(1L, request)).thenReturn(new MovieRecordResponse(
                new RatingResponse(3L, 10L, 4, null, null, null),
                new FavoriteResponse(7L, 10L, null)));

        mockMvc.perform(post("/movie-records").header("Authorization", "Bearer token")
                        .contentType("application/json")
                        .content("{\"movieId\":10,\"score\":4,\"addToFavorites\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.rating.ratingId").value(3))
                .andExpect(jsonPath("$.result.favorite.favoriteId").value(7));
        verify(movieRecordService).create(1L, request);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"score\":4,\"addToFavorites\":true}",
            "{\"movieId\":0,\"score\":4,\"addToFavorites\":true}",
            "{\"movieId\":10,\"addToFavorites\":true}",
            "{\"movieId\":10,\"score\":0,\"addToFavorites\":true}",
            "{\"movieId\":10,\"score\":6,\"addToFavorites\":true}",
            "{\"movieId\":10,\"score\":4}"
    })
    void rejectsInvalidRequests(String body) throws Exception {
        when(jwtUtil.getMemberId("token")).thenReturn(1L);
        mockMvc.perform(post("/movie-records").header("Authorization", "Bearer token")
                        .contentType("application/json").content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(movieRecordService);
    }
}
