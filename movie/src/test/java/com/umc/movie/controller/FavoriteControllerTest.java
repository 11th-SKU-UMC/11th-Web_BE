package com.umc.movie.controller;

import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.dto.FavoriteStatusResponse;
import com.umc.movie.service.FavoriteService;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoriteController.class)
class FavoriteControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean FavoriteService favoriteService;
    @MockitoBean JwtUtil jwtUtil;

    @Test
    void endpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/favorites")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/favorites/10")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/favorites").contentType("application/json").content("{\"movieId\":10}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/favorites/10")).andExpect(status().isUnauthorized());
        verifyNoInteractions(favoriteService);
    }

    @Test
    void readResponsesMatchFrontendContract() throws Exception {
        when(jwtUtil.getMemberId("token")).thenReturn(1L);
        when(favoriteService.getMyFavorites(1L)).thenReturn(List.of(new FavoriteResponse(7L, 10L, null)));
        when(favoriteService.getStatus(1L, 10L)).thenReturn(new FavoriteStatusResponse(true));
        mockMvc.perform(get("/favorites").header("Authorization", "Bearer token"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result[0].movieId").value(10));
        mockMvc.perform(get("/favorites/10").header("Authorization", "Bearer token"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result.isFavorite").value(true));
    }

    @Test
    void createAndDeleteUseTokenMember() throws Exception {
        when(jwtUtil.getMemberId("token")).thenReturn(1L);
        when(favoriteService.create(1L, new CreateFavoriteRequest(10L))).thenReturn(new FavoriteResponse(7L, 10L, null));
        mockMvc.perform(post("/favorites").header("Authorization", "Bearer token")
                        .contentType("application/json").content("{\"movieId\":10}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.result.favoriteId").value(7));
        mockMvc.perform(delete("/favorites/10").header("Authorization", "Bearer token"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.isSuccess").value(true));
        verify(favoriteService).delete(1L, 10L);
    }

    @Test
    void invalidMovieIdIsRejected() throws Exception {
        when(jwtUtil.getMemberId("token")).thenReturn(1L);
        mockMvc.perform(post("/favorites").header("Authorization", "Bearer token")
                        .contentType("application/json").content("{\"movieId\":0}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(favoriteService);
    }
}
