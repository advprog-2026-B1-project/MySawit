package com.b1.mysawit.auth;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.b1.mysawit.auth.config.SecurityConfig;
import com.b1.mysawit.auth.controller.AppController;
import com.b1.mysawit.auth.service.AuthService;
import com.b1.mysawit.auth.service.CustomOAuth2UserService;
import com.b1.mysawit.auth.service.UserService;
import com.b1.mysawit.domain.User;

@WebMvcTest(controllers = AppController.class)
@Import(SecurityConfig.class)
public class MySawitAuthTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @MockitoBean 
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    void testRegisterSuccess() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("buruh1");
        mockUser.setRole(User.Role.Buruh);
        
        when(authService.register(any())).thenReturn(mockUser);

        String requestJson = """
            {
                "nama": "Budi Buruh",
                "username": "buruh1",
                "email": "budi@mysawit.com",
                "password": "password123",
                "role": "Buruh"
            }
            """;
        
        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    void testAdminUpdateUser() throws Exception {
        User updatedUser = new User();
        updatedUser.setId(2L);
        updatedUser.setUsername("buruh_updated");
        
        // Mock service agar selalu mengembalikan updatedUser saat dipanggil dengan ID 2
        when(userService.updateUser(eq(2L), any())).thenReturn(updatedUser);

        String updateJson = """
            {
                "nama": "Budi Buruh Updated",
                "username": "buruh_updated",
                "email": "budi.baru@mysawit.com",
                "password": "passwordbaru",
                "role": "Buruh"
            }
            """;

        mockMvc.perform(put("/api/admin/users/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    void testAssignWorkerToMandor() throws Exception {
        // Asumsikan UserService.assignWorkerToMandor mengembalikan object WorkerAssignment atau void
        
        String assignJson = """
            {
                "workerId": 10,
                "mandorId": 5
            }
            """;

        mockMvc.perform(post("/api/admin/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(assignJson))
                .andExpect(status().isOk());
    }

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