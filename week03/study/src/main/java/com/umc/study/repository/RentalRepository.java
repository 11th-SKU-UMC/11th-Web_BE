package com.umc.study.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RentalRepository {

    private final JdbcTemplate jdbcTemplate;

    public void save(Map<String, Object> body) {
        // 대여일은 현재 시각, 반납 예정일은 현재 시각에서 7일 뒤로 설정합니다.
        String sql = "INSERT INTO rental (user_id, book_id, rented_at, due_at) "
                + "VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY))";

        // 사용자 ID와 도서 ID는 파라미터로 안전하게 바인딩합니다.
        jdbcTemplate.update(sql, body.get("userId"), body.get("bookId"));
    }
}
