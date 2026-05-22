package com.b1.mysawit.kebun.controller;

import com.b1.mysawit.auth.config.SecurityConfig;
import com.b1.mysawit.auth.service.CustomOAuth2UserService;
import com.b1.mysawit.auth.facade.UserSummary;
import com.b1.mysawit.config.MethodSecurityConfig;
import com.b1.mysawit.kebun.dto.KebunDashboardItem;
import com.b1.mysawit.kebun.dto.KebunDetailResponse;
import com.b1.mysawit.kebun.service.KebunService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import com.b1.mysawit.kebun.dto.KebunResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = KebunController.class)
@Import({SecurityConfig.class, MethodSecurityConfig.class})
class KebunControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private KebunService kebunService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean private AuthenticationManager authenticationManager;
    @MockitoBean private com.b1.mysawit.config.DeliveryFeatureInterceptor deliveryFeatureInterceptor;

    @Test
    @WithMockUser(authorities = "Admin")
    void testGetKebunDetail_ReturnsDetailWithMandorAndSupir() throws Exception {
        UserSummary mandor = UserSummary.builder()
                .id(10L).nama("Budi Mandor").email("budi@test.com").build();
        UserSummary supir = UserSummary.builder()
                .id(20L).nama("Andi Supir").email("andi@test.com").build();

        KebunDetailResponse response = KebunDetailResponse.builder()
                .id(1L)
                .kodeKebun("KB-001")
                .namaKebun("Kebun Alpha")
                .luasHektare(new BigDecimal("50.0"))
                .mandor(mandor)
                .supirList(List.of(supir))
                .build();

        when(kebunService.getKebunDetail(1L, null)).thenReturn(response);

        mockMvc.perform(get("/api/kebun/1/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kodeKebun").value("KB-001"))
                .andExpect(jsonPath("$.mandor.nama").value("Budi Mandor"))
                .andExpect(jsonPath("$.supirList[0].nama").value("Andi Supir"));
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testGetDashboard_ReturnsAggregatedCounts() throws Exception {
        KebunDashboardItem item = KebunDashboardItem.builder()
                .id(1L).kodeKebun("KB-001").namaKebun("Kebun Alpha")
                .luasHektare(new BigDecimal("50.0"))
                .countMandorAktif(1L).countSupirAktif(3L).build();

        when(kebunService.getDashboard(false)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/kebun/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].kodeKebun").value("KB-001"))
                .andExpect(jsonPath("$[0].countMandorAktif").value(1))
                .andExpect(jsonPath("$[0].countSupirAktif").value(3));
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testGetDashboard_NaiveStrategy() throws Exception {
        when(kebunService.getDashboard(true)).thenReturn(List.of());

        mockMvc.perform(get("/api/kebun/dashboard?naive=true"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testCreateKebun_Returns201() throws Exception {
        KebunResponse response = KebunResponse.builder()
                .id(1L).kodeKebun("KB-001").namaKebun("Kebun Alpha")
                .luasHektare(new BigDecimal("4.00")).build();
        when(kebunService.createKebun(any())).thenReturn(response);

        mockMvc.perform(post("/api/kebun")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kodeKebun\":\"KB-001\",\"namaKebun\":\"Kebun Alpha\",\"koordinat\":\"[(0,0),(200,0),(200,200),(0,200)]\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kodeKebun").value("KB-001"));
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testGetAllKebun_ReturnsList() throws Exception {
        KebunResponse response = KebunResponse.builder()
                .id(1L).kodeKebun("KB-001").namaKebun("Kebun Alpha").build();
        when(kebunService.getAllKebun(null, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/kebun"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].kodeKebun").value("KB-001"));
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testGetKebunById_ReturnsKebun() throws Exception {
        KebunResponse response = KebunResponse.builder()
                .id(1L).kodeKebun("KB-001").namaKebun("Kebun Alpha").build();
        when(kebunService.getKebunById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/kebun/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kodeKebun").value("KB-001"));
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testUpdateKebun_ReturnsUpdated() throws Exception {
        KebunResponse response = KebunResponse.builder()
                .id(1L).kodeKebun("KB-001").namaKebun("Kebun Updated").build();
        when(kebunService.updateKebun(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/kebun/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"namaKebun\":\"Kebun Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.namaKebun").value("Kebun Updated"));
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testDeleteKebun_Returns204() throws Exception {
        doNothing().when(kebunService).deleteKebun(1L);
        mockMvc.perform(delete("/api/kebun/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testAssignMandor_Returns204() throws Exception {
        doNothing().when(kebunService).assignMandor(any(), any());
        mockMvc.perform(post("/api/kebun/assign-mandor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mandorId\":10,\"kebunId\":1}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testAssignSupir_Returns204() throws Exception {
        doNothing().when(kebunService).assignSupir(any(), any());
        mockMvc.perform(post("/api/kebun/assign-supir")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"supirId\":20,\"kebunId\":1}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testReassignMandor_Returns204() throws Exception {
        doNothing().when(kebunService).reassignMandor(any(), any(), any());
        mockMvc.perform(post("/api/kebun/reassign-mandor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mandorId\":10,\"oldKebunId\":1,\"newKebunId\":2}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testReassignSupir_Returns204() throws Exception {
        doNothing().when(kebunService).reassignSupir(any(), any(), any());
        mockMvc.perform(post("/api/kebun/reassign-supir")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"supirId\":20,\"oldKebunId\":1,\"newKebunId\":2}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testUnauthorized_Returns401() throws Exception {
        mockMvc.perform(get("/api/kebun"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void testGetKebunDetail_WithSearchFilter() throws Exception {
        KebunDetailResponse response = KebunDetailResponse.builder()
                .id(1L).kodeKebun("KB-001").namaKebun("Kebun Alpha")
                .supirList(List.of())
                .build();

        when(kebunService.getKebunDetail(1L, "andi")).thenReturn(response);

        mockMvc.perform(get("/api/kebun/1/detail?searchNamaSupir=andi"))
                .andExpect(status().isOk());
    }
}
