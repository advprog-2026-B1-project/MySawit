package com.b1.mysawit.auth;

import com.b1.mysawit.auth.controller.AppController;
import com.b1.mysawit.auth.service.AuthService;
import com.b1.mysawit.auth.service.CustomOAuth2UserService;
import com.b1.mysawit.auth.service.UserService;
import com.b1.mysawit.auth.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AppController.class)
@Import(SecurityConfig.class)
class AppControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AuthService authService;
    @MockitoBean private UserService userService;
    @MockitoBean private CustomOAuth2UserService customOAuth2UserService;

    @Test
    void testLogin_Success() throws Exception {
        when(authService.login("test@mail.com", "password123")).thenReturn(true);

        String loginJson = """
            {
                "email": "test@mail.com",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk());
    }

    @Test
    void testLogin_Failed() throws Exception {
        when(authService.login("test@mail.com", "salahpass")).thenReturn(false);

        String loginJson = """
            {
                "email": "test@mail.com",
                "password": "salahpass"
            }
            """;

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isUnauthorized()); 
    }
}