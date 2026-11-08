package com.umc.movie.service;

import com.umc.movie.dto.LoginRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService authService;

    @Test
    void unknownEmailAndWrongPasswordUseSameError() {
        LoginRequest unknown = new LoginRequest("absent@example.com", "wrong");
        when(memberRepository.findByEmail(unknown.email())).thenReturn(Optional.empty());
        assertEquals("AUTH401_1", assertThrows(ApiException.class,
                () -> authService.login(unknown)).getCode());

        LoginRequest wrong = new LoginRequest("member@example.com", "wrong");
        Member member = Member.builder().email(wrong.email()).password("hash").nickname("tester").build();
        when(memberRepository.findByEmail(wrong.email())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);
        assertEquals("AUTH401_1", assertThrows(ApiException.class,
                () -> authService.login(wrong)).getCode());
    }
}
