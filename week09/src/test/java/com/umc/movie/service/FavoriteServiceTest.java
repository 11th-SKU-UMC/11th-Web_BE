// src/test/java/com/umc/movie/service/FavoriteServiceTest.java
package com.umc.movie.service;

import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // @Mock이 붙은 가짜 객체를 만들어주는 확장입니다
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository; // 진짜 DB 대신 들어가는 가짜 Repository

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private FavoriteService favoriteService; // 위의 가짜 두 개를 주입받은 진짜 Service

    private final Member member = Member.builder()
            .email("challenger@umc.com")
            .password("암호화된비밀번호")
            .nickname("영화조아")
            .build();

    @Test
    @DisplayName("아직 즐겨찾기하지 않은 영화면 즐겨찾기를 저장한다")
    void createFavorite() {
        // given: 회원은 존재하고, 아직 이 영화를 즐겨찾기하지 않은 상황
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(favoriteRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(false);
        given(favoriteRepository.save(any(Favorite.class)))
                .willReturn(Favorite.builder().member(member).movieId(550L).build());

        // when: 즐겨찾기를 등록하면
        FavoriteResponse response = favoriteService.create(1L, new CreateFavoriteRequest(550L));

        // then: 저장된 영화 ID가 응답에 담기고, 저장이 실제로 호출된다
        assertThat(response.movieId()).isEqualTo(550L);
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @DisplayName("이미 즐겨찾기한 영화면 FAVORITE409_1 예외를 던지고 저장하지 않는다")
    void createFavoriteWhenDuplicated() {
        // given: 같은 회원이 같은 영화를 이미 즐겨찾기해 둔 상황
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(favoriteRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(true);

        // when & then: 예외가 나고, 저장은 시도조차 하지 않는다
        assertThatThrownBy(() -> favoriteService.create(1L, new CreateFavoriteRequest(550L)))
                .isInstanceOf(ApiException.class)
                .hasMessage("이미 이 영화를 즐겨찾기하셨습니다.");

        verify(favoriteRepository, never()).save(any(Favorite.class));
    }
}