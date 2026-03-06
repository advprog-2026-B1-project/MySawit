package com.b1.mysawit.auth;

import com.b1.mysawit.auth.controller.AppController;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.dto.LoginRequest;
import com.b1.mysawit.auth.service.AuthService;
import com.b1.mysawit.auth.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppController.class)
public class MySawitAuthTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @Test
    public void testLoginSuccess() throws Exception {
        Mockito.when(authService.login("test@email.com", "password123")).thenReturn(true);

        String loginJson = "{\"email\":\"test@email.com\",\"password\":\"password123\"}";

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testAdminCannotDeleteThemself() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Admin Utama tidak dapat menghapus dirinya sendiri"))
               .when(userService).deleteUser(1L, 1L);

        mockMvc.perform(delete("/api/admin/users/1?currentAdminId=1"))
                .andExpect(status().isBadRequest()); 
    }
}