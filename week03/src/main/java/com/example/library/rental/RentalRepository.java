package com.example.library.rental;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RentalRepository {

    private final JdbcTemplate jdbcTemplate;

    public RentalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(RentalRequest request) {
        String sql = """
                INSERT INTO rental (user_id, book_id, rented_at, due_at)
                VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY))
                """;

        jdbcTemplate.update(sql, request.userId(), request.bookId());
    }
}
