
package com.umc.movie.service;

import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RatingService ratingService;

    @Test
    @DisplayName("같은 영화에 평점을 중복 등록하면 RATING409_1 예외가 발생하고 저장하지 않는다")
    void createRatingWhenDuplicated() {
        // given
        Long memberId = 1L;
        Long movieId = 550L;

        Member member = Member.builder()
                .email("challenger@umc.com")
                .nickname("영화조아")
                .password("test-password")
                .build();

        CreateRatingRequest request =
                new CreateRatingRequest(movieId, 4, "재미있는 영화");

        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(member));

        when(ratingRepository.existsByMember_MemberIdAndMovieId(memberId, movieId))
                .thenReturn(true);

        // when
        ApiException exception = assertThrows(
                ApiException.class,
                () -> ratingService.create(memberId, request)
        );

        // then
        assertEquals("RATING409_1", exception.getCode());

        verify(ratingRepository, never())
                .save(any(Rating.class));
    }

    @Test
    @DisplayName("다른 회원의 평점을 수정하면 RATING403_1 예외가 발생하고 저장하지 않는다")
    void updateRatingOfAnotherMember() {
        // given
        Long ownerId = 1L;
        Long requesterId = 2L;
        Long ratingId = 10L;

        Member owner = Member.builder()
                .email("owner@umc.com")
                .nickname("작성자")
                .password("test-password")
                .build();

        ReflectionTestUtils.setField(owner, "memberId", ownerId);

        Rating rating = Rating.builder()
                .member(owner)
                .movieId(550L)
                .score(4)
                .comment("기존 평점")
                .build();

        UpdateRatingRequest request =
                new UpdateRatingRequest(2, "수정 시도");

        when(ratingRepository.findById(ratingId))
                .thenReturn(Optional.of(rating));

        // when
        ApiException exception = assertThrows(
                ApiException.class,
                () -> ratingService.update(requesterId, ratingId, request)
        );

        // then
        assertEquals("RATING403_1", exception.getCode());

        verify(ratingRepository, never())
                .save(any(Rating.class));
    }
}
