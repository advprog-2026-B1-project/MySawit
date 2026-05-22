package com.b1.mysawit.auth;

import com.b1.mysawit.auth.config.SecurityConfig;
import com.b1.mysawit.auth.controller.AppController;
import com.b1.mysawit.auth.dto.UserResponse;
import com.b1.mysawit.auth.service.AuthService;
import com.b1.mysawit.auth.service.CustomOAuth2UserService;
import com.b1.mysawit.auth.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AppController.class)
@Import(SecurityConfig.class)
public class MySawitAuthTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AuthService authService;
    @MockitoBean private UserService userService;
    @MockitoBean private CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private AuthenticationManager authenticationManager;
    @MockitoBean private com.b1.mysawit.config.DeliveryFeatureInterceptor deliveryFeatureInterceptor;

    @Test
    void testRegisterSuccess() throws Exception {
        UserResponse mockUser = UserResponse.builder()
                .id(1L).username("buruh1").role("Buruh").build();

        when(authService.register(any())).thenReturn(mockUser);

        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nama":"Budi Buruh","username":"buruh1","email":"budi@mysawit.com",
                     "password":"password123","role":"Buruh"}
                    """))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testAdminUpdateUser() throws Exception {
        UserResponse updatedUser = UserResponse.builder()
                .id(2L).username("buruh_updated").role("Buruh").build();

        when(userService.updateUser(eq(2L), any())).thenReturn(updatedUser);

        mockMvc.perform(put("/api/admin/users/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nama":"Budi Updated","username":"buruh_updated",
                     "email":"budi.baru@mysawit.com","password":"passwordbaru","role":"Buruh"}
                    """))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testAssignWorkerToMandor() throws Exception {
        mockMvc.perform(post("/api/admin/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"workerId":10,"mandorId":5}
                    """))
                .andExpect(status().isOk());
    }

    @Test
    public void testLoginSuccess() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken("test@email.com", null);
        Mockito.when(authenticationManager.authenticate(any())).thenReturn(auth);

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@email.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "Admin")
    public void testAdminCannotDeleteThemself() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Admin Utama tidak dapat menghapus dirinya sendiri"))
               .when(userService).deleteUser(1L, 1L);

        mockMvc.perform(delete("/api/admin/users/1?currentAdminId=1"))
                .andExpect(status().isBadRequest());
    }
}
