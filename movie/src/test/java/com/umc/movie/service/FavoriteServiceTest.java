package com.umc.movie.service;

import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {
    @Mock FavoriteRepository favoriteRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks FavoriteService favoriteService;

    @Test
    void duplicateFavoriteIsRejected() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(mock(Member.class)));
        when(favoriteRepository.existsByMember_MemberIdAndMovieId(1L, 10L)).thenReturn(true);
        assertEquals("FAVORITE409_1", assertThrows(ApiException.class,
                () -> favoriteService.create(1L, new CreateFavoriteRequest(10L))).getCode());
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void createReturnsSavedMovie() {
        Member member = mock(Member.class);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals(10L, favoriteService.create(1L, new CreateFavoriteRequest(10L)).movieId());
    }

    @Test
    void listAndStatusUseAuthenticatedMember() {
        when(favoriteRepository.findByMember_MemberIdOrderByFavoriteIdDesc(2L))
                .thenReturn(List.of(Favorite.builder().member(mock(Member.class)).movieId(20L).build()));
        when(favoriteRepository.existsByMember_MemberIdAndMovieId(2L, 20L)).thenReturn(true);
        assertEquals(20L, favoriteService.getMyFavorites(2L).get(0).movieId());
        assertTrue(favoriteService.getStatus(2L, 20L).isFavorite());
        assertFalse(favoriteService.getStatus(2L, 99L).isFavorite());
    }

    @Test
    void deleteOnlyFindsCurrentMembersFavorite() {
        Favorite favorite = Favorite.builder().member(mock(Member.class)).movieId(10L).build();
        when(favoriteRepository.findByMember_MemberIdAndMovieId(1L, 10L)).thenReturn(Optional.of(favorite));
        favoriteService.delete(1L, 10L);
        verify(favoriteRepository).delete(favorite);
        assertEquals(404, assertThrows(ApiException.class, () -> favoriteService.delete(2L, 10L)).getStatus().value());
        verify(favoriteRepository, times(1)).delete(any());
    }
}
