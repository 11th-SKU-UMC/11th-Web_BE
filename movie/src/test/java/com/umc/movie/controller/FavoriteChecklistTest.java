package com.umc.movie.controller;

import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.service.FavoriteService;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FavoriteChecklistTest {
    private final FavoriteRepository favorites = mock(FavoriteRepository.class);
    private final MemberRepository members = mock(MemberRepository.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        JwtUtil jwt = mock(JwtUtil.class);
        when(jwt.getMemberId("token")).thenReturn(1L);
        mvc = MockMvcBuilders.standaloneSetup(new FavoriteController(new FavoriteService(favorites, members)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addInterceptors(new AuthInterceptor(jwt))
                .build();
    }

    @Test
    void tokenMemberCreatesChecksDuplicatesAndDeletesSameMovie() throws Exception {
        Member member = Member.builder().email("member@example.com").nickname("member").password("password").build();
        Map<Long, Favorite> stored = new HashMap<>();
        when(members.findById(1L)).thenReturn(Optional.of(member));
        when(favorites.existsByMember_MemberIdAndMovieId(1L, 10L)).thenAnswer(i -> stored.containsKey(10L));
        when(favorites.save(any(Favorite.class))).thenAnswer(i -> {
            Favorite favorite = i.getArgument(0);
            assertSame(member, favorite.getMember());
            ReflectionTestUtils.setField(favorite, "favoriteId", 7L);
            stored.put(favorite.getMovieId(), favorite);
            return favorite;
        });
        when(favorites.findByMember_MemberIdAndMovieId(1L, 10L))
                .thenAnswer(i -> Optional.ofNullable(stored.get(10L)));
        doAnswer(i -> { stored.remove(((Favorite) i.getArgument(0)).getMovieId()); return null; })
                .when(favorites).delete(any(Favorite.class));

        mvc.perform(get("/favorites/10").header("Authorization", "Bearer token"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result.isFavorite").value(false));
        mvc.perform(post("/favorites").header("Authorization", "Bearer token")
                        .contentType("application/json").content("{\"movieId\":10}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.result.movieId").value(10));
        mvc.perform(get("/favorites/10").header("Authorization", "Bearer token"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result.isFavorite").value(true));
        mvc.perform(post("/favorites").header("Authorization", "Bearer token")
                        .contentType("application/json").content("{\"movieId\":10}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("FAVORITE409_1"));
        mvc.perform(delete("/favorites/10").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("즐겨찾기를 해제했습니다."))
                .andExpect(jsonPath("$.result").isEmpty());
        mvc.perform(get("/favorites/10").header("Authorization", "Bearer token"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result.isFavorite").value(false));
        verify(favorites, times(1)).save(any());
        verify(members, times(2)).findById(1L);
        assertTrue(stored.isEmpty());
    }

    @Test
    void listUsesTokenMemberAndPreservesRepositoryDescendingOrder() throws Exception {
        Favorite older = Favorite.builder().member(mock(Member.class)).movieId(10L).build();
        Favorite newer = Favorite.builder().member(mock(Member.class)).movieId(20L).build();
        ReflectionTestUtils.setField(older, "favoriteId", 1L);
        ReflectionTestUtils.setField(newer, "favoriteId", 2L);
        when(favorites.findByMember_MemberIdOrderByFavoriteIdDesc(1L)).thenReturn(List.of(newer, older));

        mvc.perform(get("/favorites").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].favoriteId").value(2))
                .andExpect(jsonPath("$.result[1].favoriteId").value(1));
        verify(favorites).findByMember_MemberIdOrderByFavoriteIdDesc(1L);
        verifyNoMoreInteractions(favorites);

        PartTree query = new PartTree("findByMember_MemberIdOrderByFavoriteIdDesc", Favorite.class);
        assertEquals("member.memberId", query.getParts().iterator().next().getProperty().toDotPath());
        assertEquals(Sort.by(Sort.Direction.DESC, "favoriteId"), query.getSort());
    }
}
