package com.umc.movie.service;

import com.umc.movie.dto.CreateMovieRecordRequest;
import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MovieRecordTransactionTest {
    private final MemberRepository members = mock(MemberRepository.class);
    private final RatingRepository ratings = mock(RatingRepository.class);
    private final FavoriteRepository favorites = mock(FavoriteRepository.class);
    private final RecordingTransactionManager transactions = new RecordingTransactionManager();
    private final AtomicReference<TransactionStatus> ratingTransaction = new AtomicReference<>();
    private MovieRecordService service;

    @BeforeEach
    void setUp() {
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(transactions);
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        ProxyFactory proxy = new ProxyFactory(new MovieRecordService(members, ratings, favorites));
        proxy.addAdvice(interceptor);
        service = (MovieRecordService) proxy.getProxy();
        when(members.findById(1L)).thenReturn(Optional.of(mock(Member.class)));
        when(ratings.save(any(Rating.class))).thenAnswer(i -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            ratingTransaction.set(TransactionAspectSupport.currentTransactionStatus());
            return i.getArgument(0);
        });
    }

    @Test
    void bothSavesShareOneTransactionAndCommitTogether() {
        when(favorites.save(any(Favorite.class))).thenAnswer(i -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            assertSame(ratingTransaction.get(), TransactionAspectSupport.currentTransactionStatus());
            return i.getArgument(0);
        });

        service.create(1L, new CreateMovieRecordRequest(10L, 4, null, true));

        assertEquals(1, transactions.begins);
        assertEquals(1, transactions.commits);
        assertEquals(0, transactions.rollbacks);
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }

    @Test
    void favoriteSaveFailureRollsBackTransactionContainingRatingSave() {
        RuntimeException failure = new IllegalStateException("favorite save failed");
        when(favorites.save(any(Favorite.class))).thenThrow(failure);

        assertSame(failure, assertThrows(RuntimeException.class,
                () -> service.create(1L, new CreateMovieRecordRequest(10L, 4, null, true))));

        verify(ratings).save(any(Rating.class));
        assertNotNull(ratingTransaction.get());
        assertEquals(1, transactions.begins);
        assertEquals(0, transactions.commits);
        assertEquals(1, transactions.rollbacks);
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }

    // 실제 DB 대신 트랜잭션 경계와 commit/rollback 호출을 기록합니다.
    private static class RecordingTransactionManager extends AbstractPlatformTransactionManager {
        int begins;
        int commits;
        int rollbacks;

        @Override protected Object doGetTransaction() { return new Object(); }
        @Override protected void doBegin(Object transaction, TransactionDefinition definition) { begins++; }
        @Override protected void doCommit(DefaultTransactionStatus status) { commits++; }
        @Override protected void doRollback(DefaultTransactionStatus status) { rollbacks++; }
    }
}
