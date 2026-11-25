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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

// Rating 관련 시그니처 및 getErrorCode().getCode()는 기존 코드에 맞게 조정
@ExtendWith(MockitoExtension.class)
class RatingServiceTest {
    @Mock RatingRepository ratingRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks RatingService ratingService;

    @Test
    @DisplayName("같은 영화에 평점을 중복 등록하면 RATING409_1을 반환한다")
    void duplicateRating() {
        // given
        Member member = mock(Member.class);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(true);
        // when & then
        assertThatThrownBy(() -> ratingService.create(1L, new CreateRatingRequest(550L, 4)))
            .isInstanceOfSatisfying(ApiException.class, e ->
                org.assertj.core.api.Assertions.assertThat(e.getErrorCode().getCode()).isEqualTo("RATING409_1"));
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    @DisplayName("다른 회원의 평점을 수정하면 RATING403_1을 반환한다")
    void updateOtherMembersRating() {
        // given
        Member owner = mock(Member.class);
        Rating rating = mock(Rating.class);
        given(owner.getMemberId()).willReturn(2L);
        given(rating.getMember()).willReturn(owner);
        given(ratingRepository.findById(10L)).willReturn(Optional.of(rating));
        // when & then
        assertThatThrownBy(() -> ratingService.update(1L, 10L, new UpdateRatingRequest(5)))
            .isInstanceOfSatisfying(ApiException.class, e ->
                org.assertj.core.api.Assertions.assertThat(e.getErrorCode().getCode()).isEqualTo("RATING403_1"));
        verify(ratingRepository, never()).save(any(Rating.class));
    }
}
