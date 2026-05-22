package com.b1.mysawit.auth;

import com.b1.mysawit.auth.controller.AppController;
import com.b1.mysawit.auth.dto.UserResponse;
import com.b1.mysawit.auth.service.AuthService;
import com.b1.mysawit.auth.service.CustomOAuth2UserService;
import com.b1.mysawit.auth.service.UserService;
import com.b1.mysawit.auth.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AppController.class)
@Import(SecurityConfig.class)
class AppControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AuthService authService;
    @MockitoBean private UserService userService;
    @MockitoBean private CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private AuthenticationManager authenticationManager;
    @MockitoBean private com.b1.mysawit.config.DeliveryFeatureInterceptor deliveryFeatureInterceptor;

    @Test
    void testLogin_Success() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken("test@mail.com", null);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@mail.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testLogin_Failed() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@mail.com\",\"password\":\"salahpass\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    void testGetCurrentUser() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L).email("user@mail.com").nama("User").role("Buruh").build();
        when(userService.getUserByEmail("user@mail.com")).thenReturn(response);

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@mail.com"))
                .andExpect(jsonPath("$.role").value("Buruh"));
    }

    @Test
    @WithMockUser
    void testGetUsersByRole() throws Exception {
        when(userService.getUsersByRole("Mandor")).thenReturn(java.util.List.of(
                UserResponse.builder().id(1L).role("Mandor").build()
        ));

        mockMvc.perform(get("/api/users?role=Mandor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("Mandor"));
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testCreateUserByAdmin() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(2L).email("new@mysawit.com").role("Buruh").build();
        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nama":"Budi","username":"budi",
                     "email":"new@mysawit.com","password":"password123","role":"Buruh"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@mysawit.com"));
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testGetAllUsers_WithFilters() throws Exception {
        when(userService.getAllUsers("Budi", "mysawit", "Buruh")).thenReturn(java.util.List.of(
                UserResponse.builder().id(1L).nama("Budi").email("budi@mysawit.com").role("Buruh").build()
        ));

        mockMvc.perform(get("/api/admin/users")
                .param("nama", "Budi")
                .param("email", "mysawit")
                .param("role", "Buruh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nama").value("Budi"));
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testDeleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/users/2")
                .param("currentAdminId", "1"))
                .andExpect(status().isOk());

        verify(userService).deleteUser(eq(2L), eq(1L));
    }
}
