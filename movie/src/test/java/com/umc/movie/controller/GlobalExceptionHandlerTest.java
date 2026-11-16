package com.umc.movie.controller;

import com.umc.movie.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;

import java.sql.SQLIntegrityConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void databaseEmailUniqueViolationMatchesPrecheckResponse() {
        assertConflict("Duplicate entry 'nickname@example.com' for key 'member.email'",
                "MEMBER4091", "이미 사용 중인 이메일입니다.");
    }

    @Test
    void databaseNicknameUniqueViolationMatchesPrecheckResponse() {
        assertConflict("Duplicate entry 'tester' for key 'member.nickname'",
                "MEMBER4092", "이미 사용 중인 닉네임입니다.");
    }

    @Test
    void databaseRatingUniqueViolationMatchesPrecheckResponse() {
        assertConflict("Duplicate entry '1-10' for key 'rating.uk_rating_member_movie'",
                "RATING4091", "이미 이 영화에 평점을 남기셨습니다.");
    }

    private void assertConflict(String sqlMessage, String code, String message) {
        SQLIntegrityConstraintViolationException sql =
                new SQLIntegrityConstraintViolationException(sqlMessage, "23000", 1062);
        DataIntegrityViolationException exception = new DataIntegrityViolationException("unique violation", sql);

        ResponseEntity<ApiResponse<Void>> response = handler.handleDataIntegrity(exception);

        assertEquals(409, response.getStatusCode().value());
        assertEquals(code, response.getBody().code());
        assertEquals(message, response.getBody().message());
    }
}
