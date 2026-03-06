package com.b1.mysawit.harvest.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class HarvestResponse {
    private Long id;
    private LocalDate tanggalPanen;
    private BigDecimal kilogram;
    private String berita;
    private String status;
}