package com.b1.mysawit.kebun.controller;

import com.b1.mysawit.auth.config.SecurityConfig;
import com.b1.mysawit.auth.service.CustomOAuth2UserService;
import com.b1.mysawit.auth.facade.UserSummary;
import com.b1.mysawit.config.MethodSecurityConfig;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = KebunController.class)
@Import({SecurityConfig.class, MethodSecurityConfig.class})
class KebunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private KebunService kebunService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean private AuthenticationManager authenticationManager;

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
