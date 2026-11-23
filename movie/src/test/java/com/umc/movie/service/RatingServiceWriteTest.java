package com.umc.movie.service;

import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @DisplayName("같은 영화에 평점을 중복 등록하면 409와 RATING409_1을 반환하고 저장하지 않는다")
    void duplicateRatingIsRejectedBeforeSaving() {
        Member member = Member.builder().email("user@example.com").password("encoded").nickname("tester").build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 10L)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class,
                () -> ratingService.create(1L, new CreateRatingRequest(10L, 5, "good")));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("RATING409_1", exception.getCode());
        verify(ratingRepository, never()).save(any());
    }

    @Test
    @DisplayName("다른 회원의 평점을 수정·삭제하면 403과 RATING403_1을 반환하고 원본을 유지한다")
    void anotherMembersRatingCannotBeUpdatedOrDeleted() {
        Member owner = mock(Member.class);
        when(owner.getMemberId()).thenReturn(1L);
        Rating rating = Rating.builder().member(owner).movieId(10L).score(4).build();
        when(ratingRepository.findById(7L)).thenReturn(Optional.of(rating));

        ApiException updateException = assertThrows(ApiException.class,
                () -> ratingService.update(7L, 2L, new UpdateRatingRequest(5, "changed")));
        assertEquals(HttpStatus.FORBIDDEN, updateException.getStatus());
        assertEquals("RATING403_1", updateException.getCode());
        ApiException deleteException = assertThrows(ApiException.class,
                () -> ratingService.delete(7L, 2L));
        assertEquals(HttpStatus.FORBIDDEN, deleteException.getStatus());
        assertEquals("RATING403_1", deleteException.getCode());
        assertEquals(4, rating.getScore());
        verify(ratingRepository, never()).save(any());
        verify(ratingRepository, never()).delete(any());
    }

    @Test
    @DisplayName("존재하지 않는 평점을 수정·삭제하면 404를 반환한다")
    void missingRatingReturns404() {
        when(ratingRepository.findById(999L)).thenReturn(Optional.empty());

        assertEquals(404, assertThrows(ApiException.class,
                () -> ratingService.update(999L, 1L, new UpdateRatingRequest(5, "changed")))
                .getStatus().value());
        assertEquals(404, assertThrows(ApiException.class,
                () -> ratingService.delete(999L, 1L)).getStatus().value());
    }
}
