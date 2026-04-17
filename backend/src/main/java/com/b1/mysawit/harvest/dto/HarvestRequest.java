package com.b1.mysawit.harvest.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;

@Data
public class HarvestRequest {
    private BigDecimal kilogram;
    private String berita;
    private List<MultipartFile> photos;
}

