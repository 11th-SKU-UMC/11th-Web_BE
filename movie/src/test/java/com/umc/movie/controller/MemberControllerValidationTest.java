package com.umc.movie.controller;

import com.umc.movie.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
