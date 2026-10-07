package com.umc.study.rental.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RentalRepository {

    private final JdbcTemplate jdbcTemplate;

    // 대여 기록을 INSERT하고, AUTO_INCREMENT로 생성된 rental_id를 반환합니다.
    public Long save(Map<String, Object> body) {
        String sql = "INSERT INTO rental (user_id, book_id, rented_at, due_at) "
                + "VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY))";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"rental_id"});
            ps.setObject(1, body.get("userId"));
            ps.setObject(2, body.get("bookId"));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    // 방금 생성된 대여 기록 한 건을 조회합니다.
    public Map<String, Object> findById(Long rentalId) {
        String sql = "SELECT * FROM rental WHERE rental_id = ?";

        return jdbcTemplate.queryForMap(sql, rentalId);
    }
}