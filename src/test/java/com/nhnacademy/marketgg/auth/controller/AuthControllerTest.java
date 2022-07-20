package com.nhnacademy.marketgg.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.marketgg.auth.config.WebSecurityConfig;
import com.nhnacademy.marketgg.auth.dto.request.EmailRequest;
import com.nhnacademy.marketgg.auth.dto.request.SignUpRequest;
import com.nhnacademy.marketgg.auth.dto.response.ExistEmailResponse;
import com.nhnacademy.marketgg.auth.exception.EmailOverlapException;
import com.nhnacademy.marketgg.auth.jwt.TokenGenerator;
import com.nhnacademy.marketgg.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(WebSecurityConfig.class)
@MockBean({
        AuthenticationManager.class,
        TokenGenerator.class,
        RedisTemplate.class
})
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    AuthService authService;

    @MockBean
    UserDetailsService userDetailsService;

    @Test
    @DisplayName("회원가입 테스트")
    void testDoSignup() throws Exception {
        SignUpRequest testSignUpRequest = new SignUpRequest();

        ReflectionTestUtils.setField(testSignUpRequest, "email", "test@test.com");
        ReflectionTestUtils.setField(testSignUpRequest, "password", "1234");
        ReflectionTestUtils.setField(testSignUpRequest, "name", "testName");

        doNothing().when(authService).signup(testSignUpRequest);

        mockMvc.perform(post("/auth/signup")
                                .contentType(APPLICATION_JSON)
                                .content(mapper.writeValueAsString(testSignUpRequest)))
               .andExpect(status().isCreated())
               .andDo(print());
    }

    @Test
    @DisplayName("회원 이메일 중복체크 사용가능")
    void testCheckEmail() throws Exception {
        EmailRequest testEmailRequest = new EmailRequest();

        ReflectionTestUtils.setField(testEmailRequest, "email", "testEmail");
        ReflectionTestUtils.setField(testEmailRequest, "isReferrer", true);

        when(authService.checkEmail(testEmailRequest))
                        .thenReturn(any(ExistEmailResponse.class));

        mockMvc.perform(post("/auth/check/email")
                                .contentType(APPLICATION_JSON)
                                .content(mapper.writeValueAsString(testEmailRequest)))
               .andExpect(status().isOk())
               .andDo(print());
    }

    @Test
    @DisplayName("회원 이메일 중복체크 예외처리")
    void testExistsEmailThrownByEmailOverlapException() throws Exception {
        EmailRequest emailRequest = new EmailRequest();

        ReflectionTestUtils.setField(emailRequest, "email", "testEmail");
        ReflectionTestUtils.setField(emailRequest, "isReferrer", false);

        when(authService.checkEmail(emailRequest))
                .thenThrow(new EmailOverlapException(emailRequest.getEmail()));

        mockMvc.perform(post("/auth/check/email")
                                .contentType(APPLICATION_JSON)
                                .content(mapper.writeValueAsString(emailRequest)))
               .andExpect(result -> assertTrue(result.getResolvedException() instanceof EmailOverlapException));
    }

}
