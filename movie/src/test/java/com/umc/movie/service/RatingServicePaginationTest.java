package com.umc.movie.service;

import com.umc.movie.dto.RatingPageResponse;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServicePaginationTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RatingService ratingService;

    @Test
    void retrievesRequestedPageSortedByNewestRating() {
        when(memberRepository.existsById(1L)).thenReturn(true);
        when(ratingRepository.findByMember_MemberId(eq(1L), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(Page.empty());

        RatingPageResponse response = ratingService.getRatingsByMember(1L, 2, 5);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(ratingRepository).findByMember_MemberId(eq(1L), pageable.capture());
        assertEquals(2, pageable.getValue().getPageNumber());
        assertEquals(5, pageable.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getValue().getSort().getOrderFor("ratingId").getDirection());
        assertEquals(0, response.totalElements());
    }

    @Test
    void rejectsNegativePage() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> ratingService.getRatingsByMember(1L, -1, 10)).getStatusCode().value());
    }
}
