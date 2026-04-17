package com.b1.mysawit.harvest.controller;

import com.b1.mysawit.domain.User;
import com.b1.mysawit.harvest.dto.HarvestResponse;
import com.b1.mysawit.harvest.service.HarvestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HarvestController.class)
class HarvestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HarvestService harvestService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/harvest - Berhasil submit harvest dengan file")
    void submitHarvest_Success() throws Exception {
        // Prepare Mock Data
        HarvestResponse response = HarvestResponse.builder()
                .id(1L)
                .status("Pending")
                .kilogram(new BigDecimal("100.5"))
                .build();

        // Mocking file upload
        MockMultipartFile photo = new MockMultipartFile(
                "photos",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image content".getBytes()
        );

        when(harvestService.createHarvest(any(User.class), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/harvest")
                        .file(photo)
                        .param("kilogram", "100.5")
                        .param("berita", "Panen lancar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Pending"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/harvest/{id}/approve - Berhasil approve")
    void approveHarvest_Success() throws Exception {
        HarvestResponse response = HarvestResponse.builder().id(1L).status("Approved").build();
        when(harvestService.approveHarvest(1L)).thenReturn(response);

        mockMvc.perform(put("/api/harvest/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Approved"));
    }

    @Test
    @DisplayName("PUT /api/harvest/{id}/reject - Berhasil reject dengan alasan")
    void rejectHarvest_Success() throws Exception {
        HarvestResponse response = HarvestResponse.builder()
                .id(1L)
                .status("Rejected")
                .rejectionReason("Foto kurang jelas")
                .build();

        var rejectRequest = Collections.singletonMap("alasan", "Foto kurang jelas");

        when(harvestService.rejectHarvest(eq(1L), anyString())).thenReturn(response);

        mockMvc.perform(put("/api/harvest/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Rejected"))
                .andExpect(jsonPath("$.rejectionReason").value("Foto kurang jelas"));
    }

    @Test
    @DisplayName("GET /api/harvest/me - Berhasil mengambil riwayat")
    void getMyHarvests_Success() throws Exception {
        HarvestResponse historyItem = HarvestResponse.builder()
                .id(1L)
                .tanggalPanen(LocalDate.now())
                .status("Approved")
                .build();

        when(harvestService.getMyHarvestHistory(any(User.class)))
                .thenReturn(List.of(historyItem));

        mockMvc.perform(get("/api/harvest/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("Approved"));
    }
}