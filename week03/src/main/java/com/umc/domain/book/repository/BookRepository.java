package com.umc.domain.book.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> findByCategoryId(Long categoryId) {
        String sql = "SELECT book_id, category_id, title, description, is_available FROM book WHERE category_id = ?";
        return jdbcTemplate.queryForList(sql, categoryId);
    }

    // 책의 대여 가능 여부 조회
    public boolean isBookAvailable(Long bookId) {
        String sql = "SELECT is_available FROM book WHERE book_id = ?";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, bookId));
    }

    // 책 대여 상태 변경 (is_available -> false)
    public void updateBookAvailability(Long bookId, boolean isAvailable) {
        String sql = "UPDATE book SET is_available = ? WHERE book_id = ?";
        jdbcTemplate.update(sql, isAvailable, bookId);
    }
}