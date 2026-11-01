package com.umc.movie.service;

import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.DuplicateError;
import com.umc.movie.exception.DuplicateResourceException;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceWriteTest {
    @Mock RatingRepository ratingRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks RatingService ratingService;

    @Test
    void duplicateRatingIsRejectedBeforeSaving() {
        Member member = Member.builder().email("user@example.com").password("encoded").nickname("tester").build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 10L)).thenReturn(true);

        assertEquals(DuplicateError.RATING,
                assertThrows(DuplicateResourceException.class,
                        () -> ratingService.create(1L, new CreateRatingRequest(10L, 5, "good"))).error());
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void anotherMembersRatingCannotBeUpdatedOrDeleted() {
        Member owner = mock(Member.class);
        when(owner.getMemberId()).thenReturn(1L);
        Rating rating = Rating.builder().member(owner).movieId(10L).score(4).build();
        when(ratingRepository.findById(7L)).thenReturn(Optional.of(rating));

        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> ratingService.update(7L, 2L, new UpdateRatingRequest(5, "changed")))
                .getStatusCode().value());
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> ratingService.delete(7L, 2L)).getStatusCode().value());
        verify(ratingRepository, never()).save(any());
        verify(ratingRepository, never()).delete(any());
    }

    @Test
    void missingRatingReturns404() {
        when(ratingRepository.findById(999L)).thenReturn(Optional.empty());

        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> ratingService.update(999L, 1L, new UpdateRatingRequest(5, "changed")))
                .getStatusCode().value());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> ratingService.delete(999L, 1L)).getStatusCode().value());
    }
}
