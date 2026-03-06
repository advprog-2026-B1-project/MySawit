package com.b1.mysawit.harvest.controller;

import com.b1.mysawit.harvest.dto.HarvestResponse;
import com.b1.mysawit.harvest.service.HarvestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HarvestController.class)
class HarvestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HarvestService harvestService;

    @Test
    void submitHarvest_ShouldReturn200() throws Exception {
        HarvestResponse response = HarvestResponse.builder()
                .id(1L).status("Pending")
                .build();

        when(harvestService.createHarvest(any(), any())).thenReturn(response);

        String jsonPayload = "{\"kilogram\": 100, \"berita\": \"Panen blok A\"}";

        mockMvc.perform(post("/api/hasil-panen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());
    }

    @Test
    void getMyHarvests_ShouldReturn200() throws Exception {
        when(harvestService.getMyHarvestHistory(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/hasil-panen/me"))
                .andExpect(status().isOk());
    }
}