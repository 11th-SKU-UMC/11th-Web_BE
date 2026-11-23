package com.umc.movie.service;

import com.umc.movie.dto.CreateMovieRecordRequest;
import com.umc.movie.dto.MovieRecordResponse;
import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieRecordServiceTest {
    @Mock MemberRepository memberRepository;
    @Mock RatingRepository ratingRepository;
    @Mock FavoriteRepository favoriteRepository;
    @InjectMocks MovieRecordService movieRecordService;

    @Test
    void savesRatingAndOptionalFavoriteForAuthenticatedMember() {
        Member member = mock(Member.class);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> {
            Rating rating = invocation.getArgument(0);
            assertSame(member, rating.getMember());
            return rating;
        });
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> {
            Favorite favorite = invocation.getArgument(0);
            assertSame(member, favorite.getMember());
            return favorite;
        });

        MovieRecordResponse response = movieRecordService.create(1L,
                new CreateMovieRecordRequest(10L, 4, "감상 기록", true));

        assertEquals(10L, response.rating().movieId());
        assertEquals(4, response.rating().score());
        assertEquals("감상 기록", response.rating().comment());
        assertEquals(10L, response.favorite().movieId());
    }

    @Test
    void skipsFavoritesWhenNotRequested() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(mock(Member.class)));
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovieRecordResponse response = movieRecordService.create(1L,
                new CreateMovieRecordRequest(10L, 5, null, false));

        assertEquals(5, response.rating().score());
        assertNull(response.favorite());
        verifyNoInteractions(favoriteRepository);
    }

    @Test
    void duplicateRatingRejectsBothWrites() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(mock(Member.class)));
        when(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 10L)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> movieRecordService.create(1L,
                new CreateMovieRecordRequest(10L, 4, null, true)));

        assertEquals("RATING409_1", exception.getCode());
        verify(ratingRepository, never()).save(any());
        verifyNoInteractions(favoriteRepository);
    }

    @Test
    void duplicateFavoriteFailsAfterRatingSaveSoTransactionMustRollBack() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(mock(Member.class)));
        when(favoriteRepository.existsByMember_MemberIdAndMovieId(1L, 10L)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> movieRecordService.create(1L,
                new CreateMovieRecordRequest(10L, 4, null, true)));

        assertEquals("FAVORITE409_1", exception.getCode());
        verify(ratingRepository).save(any(Rating.class));
        verify(favoriteRepository, never()).save(any());
    }
}
