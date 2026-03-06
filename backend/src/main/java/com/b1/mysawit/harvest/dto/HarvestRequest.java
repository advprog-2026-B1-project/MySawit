package com.b1.mysawit.harvest.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class HarvestRequest {
    private BigDecimal kilogram;
    private String berita;
}