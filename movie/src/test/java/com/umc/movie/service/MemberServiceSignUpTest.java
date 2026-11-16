package com.umc.movie.service;

import com.umc.movie.dto.MemberResponse;
import com.umc.movie.dto.SignUpRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.DuplicateError;
import com.umc.movie.exception.DuplicateResourceException;
import com.umc.movie.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceSignUpTest {
    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks MemberService memberService;

    @Test
    void signUpStoresEncodedPasswordAndReturnsPublicFields() {
        SignUpRequest request = new SignUpRequest("user@example.com", "Password1!", "tester");
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded-password");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberResponse response = memberService.signUp(request);

        assertEquals("user@example.com", response.email());
        assertEquals("tester", response.nickname());
        org.mockito.ArgumentCaptor<Member> member = org.mockito.ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(member.capture());
        assertEquals("encoded-password", member.getValue().getPassword());
    }

    @Test
    void duplicateEmailIsRejectedBeforeSaving() {
        SignUpRequest request = new SignUpRequest("user@example.com", "Password1!", "tester");
        when(memberRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertEquals(DuplicateError.EMAIL,
                assertThrows(DuplicateResourceException.class, () -> memberService.signUp(request)).error());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void duplicateNicknameHasItsOwnErrorCode() {
        SignUpRequest request = new SignUpRequest("user@example.com", "Password1!", "tester");
        when(memberRepository.existsByNickname("tester")).thenReturn(true);

        assertEquals(DuplicateError.NICKNAME,
                assertThrows(DuplicateResourceException.class, () -> memberService.signUp(request)).error());
        verify(memberRepository, never()).save(any());
    }
}
