package com.example.library.domain.rental.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class RentalRepository {

    private final JdbcTemplate jdbcTemplate;

    public RentalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Map<String, Object> body) {
        String sql = """
                INSERT INTO rental
                (user_id, book_id, rented_at, due_at)
                VALUES (?, ?, NOW(),
                DATE_ADD(NOW(), INTERVAL 7 DAY))
                """;

        jdbcTemplate.update(
                sql,
                body.get("userId"),
                body.get("bookId")
        );
    }
}
