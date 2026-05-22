package com.b1.mysawit.delivery.controller;

import com.b1.mysawit.auth.config.SecurityConfig;
import com.b1.mysawit.auth.service.CustomOAuth2UserService;
import com.b1.mysawit.config.DeliveryFeatureInterceptor;
import com.b1.mysawit.config.FeatureFlags;
import com.b1.mysawit.config.MethodSecurityConfig;
import com.b1.mysawit.delivery.dto.AdminDeliveryDecisionRequest;
import com.b1.mysawit.delivery.dto.CreateDeliveryRequest;
import com.b1.mysawit.delivery.dto.DeliveryResponse;
import com.b1.mysawit.delivery.dto.MandorDeliveryDecisionRequest;
import com.b1.mysawit.delivery.dto.UpdateDeliveryStatusRequest;
import com.b1.mysawit.delivery.service.DeliveryAuthenticationHelper;
import com.b1.mysawit.delivery.service.DeliveryService;
import com.b1.mysawit.domain.Delivery;
import com.b1.mysawit.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeliveryController.class)
@Import({SecurityConfig.class, MethodSecurityConfig.class})
class DeliveryControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeliveryService deliveryService;

    @MockBean
    private DeliveryAuthenticationHelper authenticationHelper;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private FeatureFlags featureFlags;

    @MockBean
    private DeliveryFeatureInterceptor deliveryFeatureInterceptor;

    private User dummyMandor;
    private User dummyDriver;
    private DeliveryResponse dummyResponse;

    @BeforeEach
    void setUp() throws Exception {
        dummyMandor = new User();
        dummyMandor.setId(1L);
        dummyMandor.setEmail("mandor@mysawit.com");
        dummyMandor.setRole(User.Role.Mandor);

        dummyDriver = new User();
        dummyDriver.setId(2L);
        dummyDriver.setEmail("driver@mysawit.com");
        dummyDriver.setRole(User.Role.Supir);

        dummyResponse = new DeliveryResponse(
                1L, 1L, 2L, 3L, Delivery.Status.Memuat, null,
                null, null, null, null, null, null, null, null, null
        );

        // Configure the interceptor mock to allow preHandle
        when(deliveryFeatureInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    // ==========================================
    // POST /api/delivery
    // ==========================================

    @Test
    @WithMockUser(authorities = "Mandor")
    void createDelivery_withMandorRole_shouldSucceed() throws Exception {
        CreateDeliveryRequest request = new CreateDeliveryRequest(2L, 3L);
        when(authenticationHelper.getCurrentUser()).thenReturn(dummyMandor);
        when(deliveryService.createDelivery(eq(1L), any(CreateDeliveryRequest.class))).thenReturn(dummyResponse);

        mockMvc.perform(post("/api/delivery")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "Supir")
    void createDelivery_withDriverRole_shouldFail403() throws Exception {
        CreateDeliveryRequest request = new CreateDeliveryRequest(2L, 3L);

        mockMvc.perform(post("/api/delivery")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDelivery_unauthenticated_shouldFail401() throws Exception {
        CreateDeliveryRequest request = new CreateDeliveryRequest(2L, 3L);

        mockMvc.perform(post("/api/delivery")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // PUT /api/delivery/{id}/status
    // ==========================================

    @Test
    @WithMockUser(authorities = "Supir")
    void updateStatus_withDriverRole_shouldSucceed() throws Exception {
        UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest(Delivery.Status.Mengirim);
        when(authenticationHelper.getCurrentUser()).thenReturn(dummyDriver);
        when(deliveryService.updateStatus(eq(2L), eq(1L), any(UpdateDeliveryStatusRequest.class))).thenReturn(dummyResponse);

        mockMvc.perform(put("/api/delivery/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "Mandor")
    void updateStatus_withMandorRole_shouldFail403() throws Exception {
        UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest(Delivery.Status.Mengirim);

        mockMvc.perform(put("/api/delivery/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // PUT /api/delivery/{id}/mandor-decision
    // ==========================================

    @Test
    @WithMockUser(authorities = "Mandor")
    void decideByMandor_withMandorRole_shouldSucceed() throws Exception {
        MandorDeliveryDecisionRequest request = new MandorDeliveryDecisionRequest(Delivery.MandorDecision.Approved, null);
        when(authenticationHelper.getCurrentUser()).thenReturn(dummyMandor);
        when(deliveryService.decideByMandor(eq(1L), eq(1L), any(MandorDeliveryDecisionRequest.class))).thenReturn(dummyResponse);

        mockMvc.perform(put("/api/delivery/1/mandor-decision")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "Supir")
    void decideByMandor_withDriverRole_shouldFail403() throws Exception {
        MandorDeliveryDecisionRequest request = new MandorDeliveryDecisionRequest(Delivery.MandorDecision.Approved, null);

        mockMvc.perform(put("/api/delivery/1/mandor-decision")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // PUT /api/delivery/{id}/admin-decision
    // ==========================================

    @Test
    @WithMockUser(authorities = "Admin")
    void decideByAdmin_withAdminRole_shouldSucceed() throws Exception {
        AdminDeliveryDecisionRequest request = new AdminDeliveryDecisionRequest(Delivery.AdminDecision.Approved, null, null);
        when(deliveryService.decideByAdmin(eq(1L), any(AdminDeliveryDecisionRequest.class))).thenReturn(dummyResponse);

        mockMvc.perform(put("/api/delivery/1/admin-decision")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "Mandor")
    void decideByAdmin_withMandorRole_shouldFail403() throws Exception {
        AdminDeliveryDecisionRequest request = new AdminDeliveryDecisionRequest(Delivery.AdminDecision.Approved, null, null);

        mockMvc.perform(put("/api/delivery/1/admin-decision")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // GET /api/delivery/driver
    // ==========================================

    @Test
    @WithMockUser(authorities = "Supir")
    void getDeliveriesByDriver_withDriverRole_shouldSucceed() throws Exception {
        when(authenticationHelper.getCurrentUser()).thenReturn(dummyDriver);
        when(deliveryService.getByDriver(2L)).thenReturn(List.of(dummyResponse));

        mockMvc.perform(get("/api/delivery/driver"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "Mandor")
    void getDeliveriesByDriver_withMandorRole_shouldFail403() throws Exception {
        mockMvc.perform(get("/api/delivery/driver"))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // GET /api/delivery/mandor
    // ==========================================

    @Test
    @WithMockUser(authorities = "Mandor")
    void getDeliveriesByMandor_withMandorRole_shouldSucceed() throws Exception {
        when(authenticationHelper.getCurrentUser()).thenReturn(dummyMandor);
        when(deliveryService.getByMandor(1L)).thenReturn(List.of(dummyResponse));

        mockMvc.perform(get("/api/delivery/mandor"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "Supir")
    void getDeliveriesByMandor_withDriverRole_shouldFail403() throws Exception {
        mockMvc.perform(get("/api/delivery/mandor"))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // GET /api/delivery/mandor/ongoing
    // ==========================================

    @Test
    @WithMockUser(authorities = "Mandor")
    void getMandorOngoingDeliveries_withMandorRole_shouldSucceed() throws Exception {
        LocalDate date = LocalDate.now();
        when(authenticationHelper.getCurrentUser()).thenReturn(dummyMandor);
        when(deliveryService.getMandorOngoing(eq(1L), eq(2L), any(LocalDate.class), eq("test")))
                .thenReturn(List.of(dummyResponse));

        mockMvc.perform(get("/api/delivery/mandor/ongoing")
                        .param("driverId", "2")
                        .param("date", date.toString())
                        .param("keyword", "test"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void getMandorOngoingDeliveries_withAdminRole_shouldFail403() throws Exception {
        mockMvc.perform(get("/api/delivery/mandor/ongoing"))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // GET /api/delivery/mandor/history
    // ==========================================

    @Test
    @WithMockUser(authorities = "Mandor")
    void getMandorHistoryDeliveries_withMandorRole_shouldSucceed() throws Exception {
        LocalDate date = LocalDate.now();
        when(authenticationHelper.getCurrentUser()).thenReturn(dummyMandor);
        when(deliveryService.getMandorHistory(eq(1L), eq(2L), any(LocalDate.class), eq("test")))
                .thenReturn(List.of(dummyResponse));

        mockMvc.perform(get("/api/delivery/mandor/history")
                        .param("driverId", "2")
                        .param("date", date.toString())
                        .param("keyword", "test"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void getMandorHistoryDeliveries_withAdminRole_shouldFail403() throws Exception {
        mockMvc.perform(get("/api/delivery/mandor/history"))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // GET /api/delivery/admin/ready
    // ==========================================

    @Test
    @WithMockUser(authorities = "Admin")
    void getAdminReadyDeliveries_withAdminRole_shouldSucceed() throws Exception {
        LocalDate date = LocalDate.now();
        when(deliveryService.getAdminReadyDeliveries("mandor", date)).thenReturn(List.of(dummyResponse));

        mockMvc.perform(get("/api/delivery/admin/ready")
                        .param("mandorName", "mandor")
                        .param("date", date.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "Mandor")
    void getAdminReadyDeliveries_withMandorRole_shouldFail403() throws Exception {
        mockMvc.perform(get("/api/delivery/admin/ready"))
                .andExpect(status().isForbidden());
    }
}
