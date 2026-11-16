package com.umc.movie.controller;

import com.umc.movie.dto.MemberResponse;
import com.umc.movie.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    void nicknameOutsideAllowedLengthReturnsBadRequestBeforeLookup() throws Exception {
        mockMvc.perform(get("/members/nickname/a"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(memberService);
    }

    @Test
    void malformedEmailReturnsBadRequestBeforeLookup() throws Exception {
        mockMvc.perform(get("/members/email/not-an-email"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(memberService);
    }

    @Test
    void weakSignUpPasswordReturnsValidationErrorBeforeSaving() throws Exception {
        mockMvc.perform(post("/members")
                        .contentType("application/json")
                        .content("{\"email\":\"user@example.com\",\"password\":\"weak\",\"nickname\":\"tester\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON4001"));

        verifyNoInteractions(memberService);
    }

    @Test
    void signUpResponseOmitsPassword() throws Exception {
        when(memberService.signUp(any())).thenReturn(new MemberResponse(1L, "user@example.com", "tester"));

        mockMvc.perform(post("/members")
                        .contentType("application/json")
                        .content("{\"email\":\"user@example.com\",\"password\":\"Password1!\",\"nickname\":\"tester\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.memberId").value(1))
                .andExpect(jsonPath("$.result.password").doesNotExist());
    }
}
