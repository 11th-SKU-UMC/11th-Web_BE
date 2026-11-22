package com.umc.movie.service;

import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {
    @Mock RatingRepository ratingRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks RatingService ratingService;

    @Test
    @DisplayName("같은 영화에 평점을 중복 등록하면 409와 RATING409_1 예외가 발생하고 저장하지 않는다")
    void createDuplicateRatingThrowsConflict() {
        Member member = Member.builder().email("rating@umc.com").password("encoded").nickname("평점회원").build();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(true);

        assertThatThrownBy(() -> ratingService.create(1L, new CreateRatingRequest(550L, 5, "좋아요")))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(exception.getCode()).isEqualTo("RATING409_1");
                });
        verify(ratingRepository, never()).saveAndFlush(any(Rating.class));
    }

    @Test
    @DisplayName("다른 회원의 평점을 수정하면 403과 RATING403_1 예외가 발생하고 기존 평점을 유지한다")
    void updateAnotherMembersRatingThrowsForbidden() {
        Member owner = org.mockito.Mockito.mock(Member.class);
        given(owner.getMemberId()).willReturn(1L);
        Rating rating = Rating.builder().member(owner).movieId(550L).score(5).comment("원래 후기").build();
        given(ratingRepository.findById(10L)).willReturn(Optional.of(rating));

        assertThatThrownBy(() -> ratingService.update(2L, 10L, new UpdateRatingRequest(1, "변경 시도")))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(exception.getCode()).isEqualTo("RATING403_1");
                });
        assertThat(rating.getScore()).isEqualTo(5);
        assertThat(rating.getComment()).isEqualTo("원래 후기");
        verify(ratingRepository, never()).saveAndFlush(any(Rating.class));
    }
}
