package com.b1.mysawit.harvest.controller;

import com.b1.mysawit.domain.User;
import com.b1.mysawit.harvest.dto.HarvestResponse;
import com.b1.mysawit.harvest.service.HarvestService;
import com.b1.mysawit.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = HarvestController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class HarvestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HarvestService harvestService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    private User dummyUser;
    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setRole(User.Role.Mandor);
        dummyUser.setEmail("test@example.com");

        authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(dummyUser));
    }

    @Test
    void getCurrentUser_WhenNotAuthenticated_ThrowsException() {
        when(authentication.isAuthenticated()).thenReturn(false);
        Exception ex = assertThrows(Exception.class, () -> mockMvc.perform(get("/api/harvest/me")));
        assertTrue(ex.getCause() instanceof IllegalStateException);
        assertTrue(ex.getCause().getMessage().contains("Authentication required"));
    }

    @Test
    void getCurrentUser_WhenAuthIsNull_ThrowsException() {
        when(securityContext.getAuthentication()).thenReturn(null);
        Exception ex = assertThrows(Exception.class, () -> mockMvc.perform(get("/api/harvest/me")));
        assertTrue(ex.getCause() instanceof IllegalStateException);
        assertTrue(ex.getCause().getMessage().contains("Authentication required"));
    }

    @Test
    void getCurrentUser_WithOAuth2User() throws Exception {
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn("test@example.com");
        when(authentication.getPrincipal()).thenReturn(oauth2User);

        mockMvc.perform(get("/api/harvest/me")).andExpect(status().isOk());
    }

    @Test
    void getCurrentUser_WithSpringUserDetails() throws Exception {
        org.springframework.security.core.userdetails.User springUser =
                new org.springframework.security.core.userdetails.User("test@example.com", "password", Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(springUser);

        mockMvc.perform(get("/api/harvest/me")).andExpect(status().isOk());
    }

    @Test
    void getCurrentUser_WithUnknownPrincipal_ThrowsException() {
        when(authentication.getPrincipal()).thenReturn(new Object());
        Exception ex = assertThrows(Exception.class, () -> mockMvc.perform(get("/api/harvest/me")));
        assertTrue(ex.getCause() instanceof IllegalStateException);
        assertTrue(ex.getCause().getMessage().contains("Could not extract email"));
    }

    @Test
    void getCurrentUser_UserNotFoundInDb_ThrowsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        Exception ex = assertThrows(Exception.class, () -> mockMvc.perform(get("/api/harvest/me")));
        assertTrue(ex.getCause() instanceof IllegalStateException);
        assertTrue(ex.getCause().getMessage().contains("User not found"));
    }

    @Test
    @DisplayName("GET /api/harvest/mandor - Berhasil mengambil riwayat mandor")
    void getHarvestsForMandor_Success() throws Exception {
        mockMvc.perform(get("/api/harvest/mandor?status=Pending&searchNama=Budi"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/harvest/{id}/approve - Forbidden jika bukan Mandor")
    void approveHarvest_Forbidden_WhenNotMandor() throws Exception {
        dummyUser.setRole(User.Role.Buruh); // Set role ke Buruh
        mockMvc.perform(put("/api/harvest/1/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/harvest/{id}/reject - Forbidden jika bukan Mandor")
    void rejectHarvest_Forbidden_WhenNotMandor() throws Exception {
        dummyUser.setRole(User.Role.Buruh); // Set role ke Buruh
        var rejectRequest = Collections.singletonMap("alasan", "Foto kurang jelas");

        mockMvc.perform(put("/api/harvest/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/harvest - Berhasil submit harvest dengan file")
    void submitHarvest_Success() throws Exception {
        HarvestResponse response = HarvestResponse.builder().id(1L).status("Pending").build();
        MockMultipartFile photo = new MockMultipartFile("photos", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image content".getBytes());
        when(harvestService.createHarvest(any(User.class), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/harvest")
                        .file(photo)
                        .param("kilogram", "100.5")
                        .param("berita", "Panen lancar"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/harvest/{id}/approve - Berhasil approve")
    void approveHarvest_Success() throws Exception {
        HarvestResponse response = HarvestResponse.builder().id(1L).status("Approved").build();
        when(harvestService.approveHarvest(eq(1L), any(User.class))).thenReturn(response);
        mockMvc.perform(put("/api/harvest/1/approve")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/harvest/{id}/reject - Berhasil reject dengan alasan")
    void rejectHarvest_Success() throws Exception {
        HarvestResponse response = HarvestResponse.builder().id(1L).status("Rejected").rejectionReason("Foto kurang jelas").build();
        var rejectRequest = Collections.singletonMap("alasan", "Foto kurang jelas");
        when(harvestService.rejectHarvest(eq(1L), anyString(), any(User.class))).thenReturn(response);
        mockMvc.perform(put("/api/harvest/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/harvest/me - Berhasil mengambil riwayat")
    void getMyHarvests_Success() throws Exception {
        when(harvestService.getMyHarvestHistory(any(User.class), isNull(), isNull(), isNull())).thenReturn(List.of());
        mockMvc.perform(get("/api/harvest/me")).andExpect(status().isOk());
    }
}