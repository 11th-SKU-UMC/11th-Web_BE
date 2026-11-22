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
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RatingService ratingService;

    @Test
    @DisplayName("같은 영화에 평점을 중복 등록하면 RATING409_1 예외를 던지고 저장하지 않는다")
    void createRatingWhenDuplicated() {
        // given
        Long memberId = 1L;
        Long movieId = 550L;
        Member member = Member.builder()
                .email("challenger@umc.com")
                .password("encodedPassword")
                .nickname("영화조아")
                .build();
        ReflectionTestUtils.setField(member, "memberId", memberId);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(ratingRepository.existsByMember_MemberIdAndMovieId(memberId, movieId)).willReturn(true);

        CreateRatingRequest request = new CreateRatingRequest(movieId, 5, "인생 영화");

        // when & then
        assertThatThrownBy(() -> ratingService.create(memberId, request))
                .isInstanceOf(ApiException.class)
                .hasMessage("이미 이 영화에 평점을 남기셨습니다.")
                .extracting(ex -> ((ApiException) ex).getStatus(), ex -> ((ApiException) ex).getCode())
                .containsExactly(HttpStatus.CONFLICT, "RATING409_1");

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    @DisplayName("남이 작성한 평점을 수정하려 하면 RATING403_1 예외를 던지고 저장하지 않는다")
    void updateRatingWhenNotOwner() {
        // given
        Long requesterId = 2L;
        Long ownerId = 1L;
        Long ratingId = 10L;

        Member owner = Member.builder()
                .email("owner@umc.com")
                .password("encodedPassword")
                .nickname("작성자")
                .build();
        ReflectionTestUtils.setField(owner, "memberId", ownerId);

        Rating rating = Rating.builder()
                .member(owner)
                .movieId(550L)
                .score(4)
                .comment("원래 평점")
                .build();
        ReflectionTestUtils.setField(rating, "ratingId", ratingId);

        given(ratingRepository.findById(ratingId)).willReturn(Optional.of(rating));

        UpdateRatingRequest request = new UpdateRatingRequest(1, "남의 평점 몰래 수정");

        // when & then
        assertThatThrownBy(() -> ratingService.update(requesterId, ratingId, request))
                .isInstanceOf(ApiException.class)
                .hasMessage("본인이 작성한 평점만 수정하거나 삭제할 수 있습니다.")
                .extracting(ex -> ((ApiException) ex).getStatus(), ex -> ((ApiException) ex).getCode())
                .containsExactly(HttpStatus.FORBIDDEN, "RATING403_1");

        verify(ratingRepository, never()).save(any(Rating.class));
    }
}