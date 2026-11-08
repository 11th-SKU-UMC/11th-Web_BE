package com.umc.movie.util;

import com.umc.movie.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {
    private static final String SECRET = "test-only-jwt-secret-key-with-32-plus-characters";

    @Test
    void issuedTokenResolvesMemberId() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 3600L);
        String token = jwtUtil.createAccessToken(42L, "member@example.com");
        assertEquals(42L, jwtUtil.getMemberId(token));
    }

    @Test
    void expiredTokenHasDistinctErrorCode() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, -3600L);
        String token = jwtUtil.createAccessToken(42L, "member@example.com");
        assertEquals("AUTH401_4", assertThrows(ApiException.class,
                () -> jwtUtil.getMemberId(token)).getCode());
    }

    @Test
    void malformedTokenIsRejected() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 3600L);
        assertEquals("AUTH401_3", assertThrows(ApiException.class,
                () -> jwtUtil.getMemberId("invalid.token.value")).getCode());
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String token = new JwtUtil(SECRET, 3600L).createAccessToken(42L, "member@example.com");
        JwtUtil verifier = new JwtUtil("another-test-only-secret-key-with-32-plus-characters", 3600L);
        assertEquals("AUTH401_3", assertThrows(ApiException.class,
                () -> verifier.getMemberId(token)).getCode());
    }
}
