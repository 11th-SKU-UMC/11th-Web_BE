package com.umc.study.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> findAll() {
        String sql = "SELECT * FROM book";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> findByCategoryId(long categoryId) {
        String sql = "SELECT * FROM book WHERE category_id = ?";
        return jdbcTemplate.queryForList(sql, categoryId);
    }

}
